package com.ca.module.auction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ca.common.exception.BusinessException;
import com.ca.module.auction.entity.Bid;
import com.ca.module.auction.mapper.BidMapper;
import com.ca.module.auction.service.AuctionService;
import com.ca.module.auction.vo.BidVO;
import com.ca.module.order.service.OrderService;
import com.ca.module.product.entity.Product;
import com.ca.module.product.mapper.ProductMapper;
import com.ca.module.user.entity.User;
import com.ca.module.user.mapper.UserMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {

    private final StringRedisTemplate redisTemplate;
    private final BidMapper bidMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final OrderService orderService;

    private static final String PRICE_KEY = "auction:%d:price";
    private static final String WINNER_KEY = "auction:%d:winner";
    private static final String LOCK_KEY = "auction:check:lock";

    private DefaultRedisScript<List> placeBidScript;

    @PostConstruct
    public void init() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/auction_place_bid.lua"));
        script.setResultType(List.class);
        this.placeBidScript = script;
    }

    @Override
    public BidVO placeBid(Long userId, Long productId, BigDecimal bidAmount) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw BusinessException.notFound("商品不存在");
        if (!"AUCTION".equals(product.getType())) throw BusinessException.badRequest("该商品不是拍卖商品");
        if (!"ACTIVE".equals(product.getStatus())) throw BusinessException.badRequest("拍卖已结束");
        if (product.getAuctionEnd() != null && LocalDateTime.now().isAfter(product.getAuctionEnd())) {
            throw BusinessException.badRequest("拍卖已结束");
        }
        if (product.getSellerId().equals(userId)) {
            throw BusinessException.badRequest("不能给自己的商品出价");
        }

        User bidder = userMapper.selectById(userId);
        String priceKey = String.format(PRICE_KEY, productId);
        String winnerKey = String.format(WINNER_KEY, productId);
        String startPrice = product.getStartPrice() != null ? product.getStartPrice().toString() : "0";
        String sellerId = product.getSellerId().toString();

        @SuppressWarnings("unchecked")
        List<Object> result = redisTemplate.execute(
                placeBidScript,
                List.of(priceKey, winnerKey),
                bidAmount.toString(), userId.toString(), startPrice, sellerId);

        if (result == null || result.isEmpty()) {
            throw new BusinessException(500, "出价处理异常");
        }
        long code = ((Number) result.get(0)).longValue();
        if (code == 0) {
            throw BusinessException.badRequest((String) result.get(1));
        }

        // 异步持久化出价记录
        asyncPersistBid(userId, productId, bidAmount);

        // WebSocket实时推送
        Map<String, Object> payload = Map.of(
                "productId", productId,
                "bidAmount", bidAmount,
                "currentPrice", bidAmount,
                "bidderId", userId,
                "bidderName", bidder != null ? bidder.getUsername() : "未知",
                "bidTime", LocalDateTime.now().toString()
        );
        messagingTemplate.convertAndSend("/topic/auction/" + productId,
                Map.of("type", "NEW_BID", "payload", payload, "timestamp", System.currentTimeMillis()));

        BidVO vo = BidVO.builder()
                .productId(productId)
                .bidderId(userId)
                .bidderName(bidder != null ? bidder.getUsername() : "未知")
                .bidAmount(bidAmount)
                .bidTime(LocalDateTime.now())
                .build();
        return vo;
    }

    @Async
    public void asyncPersistBid(Long userId, Long productId, BigDecimal bidAmount) {
        Bid bid = new Bid();
        bid.setProductId(productId);
        bid.setUserId(userId);
        bid.setBidAmount(bidAmount);
        bid.setBidTime(LocalDateTime.now());
        bidMapper.insert(bid);
    }

    @Override
    public IPage<BidVO> getBidHistory(Long productId, int page, int size) {
        Page<Bid> pager = new Page<>(page, size);
        LambdaQueryWrapper<Bid> wrapper = new LambdaQueryWrapper<Bid>()
                .eq(Bid::getProductId, productId)
                .orderByDesc(Bid::getBidTime);
        IPage<Bid> result = bidMapper.selectPage(pager, wrapper);
        return result.convert(bid -> {
            User user = userMapper.selectById(bid.getUserId());
            return BidVO.builder()
                    .id(bid.getId())
                    .productId(bid.getProductId())
                    .bidderId(bid.getUserId())
                    .bidderName(user != null ? user.getUsername() : "未知")
                    .bidAmount(bid.getBidAmount())
                    .bidTime(bid.getBidTime())
                    .build();
        });
    }

    @Override
    public BigDecimal getCurrentPrice(Long productId) {
        String price = redisTemplate.opsForValue().get(String.format(PRICE_KEY, productId));
        if (price != null) return new BigDecimal(price);
        Product product = productMapper.selectById(productId);
        return product != null && product.getCurrentPrice() != null
                ? product.getCurrentPrice() : BigDecimal.ZERO;
    }

    @Override
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkExpiredAuctions() {
        // Redis分布式锁防重复执行
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(LOCK_KEY, "1", 60, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) return;

        try {
            List<Product> expiredList = productMapper.selectList(
                    new LambdaQueryWrapper<Product>()
                            .eq(Product::getType, "AUCTION")
                            .eq(Product::getStatus, "ACTIVE")
                            .le(Product::getAuctionEnd, LocalDateTime.now()));

            for (Product product : expiredList) {
                String winnerKey = String.format(WINNER_KEY, product.getId());
                String priceKey = String.format(PRICE_KEY, product.getId());
                String winnerIdStr = redisTemplate.opsForValue().get(winnerKey);
                String finalPrice = redisTemplate.opsForValue().get(priceKey);

                if (winnerIdStr != null) {
                    // 成交
                    Long winnerId = Long.parseLong(winnerIdStr);
                    product.setStatus("SOLD");
                    product.setWinnerId(winnerId);
                    product.setCurrentPrice(finalPrice != null ? new BigDecimal(finalPrice) : product.getStartPrice());
                    productMapper.updateById(product);

                    // 推送成交消息
                    User winner = userMapper.selectById(winnerId);
                    messagingTemplate.convertAndSend("/topic/auction/" + product.getId(),
                            Map.of("type", "AUCTION_END", "payload", Map.of(
                                    "productId", product.getId(),
                                    "winnerId", winnerId,
                                    "winnerName", winner != null ? winner.getUsername() : "未知",
                                    "finalPrice", finalPrice != null ? finalPrice : "0",
                                    "totalBids", bidMapper.selectCount(
                                            new LambdaQueryWrapper<Bid>().eq(Bid::getProductId, product.getId()))
                            ), "timestamp", System.currentTimeMillis()));

                    // 生成订单
                    BigDecimal price = finalPrice != null ? new BigDecimal(finalPrice) : product.getStartPrice();
                    orderService.createOrderFromAuction(product.getId(), winnerId, product.getSellerId(), price);

                    log.info("拍卖成交: productId={}, winnerId={}, price={}", product.getId(), winnerId, finalPrice);
                } else {
                    // 流拍
                    product.setStatus("EXPIRED");
                    productMapper.updateById(product);

                    messagingTemplate.convertAndSend("/topic/auction/" + product.getId(),
                            Map.of("type", "AUCTION_EXPIRED", "payload",
                                    Map.of("productId", product.getId()),
                                    "timestamp", System.currentTimeMillis()));

                    log.info("拍卖流拍: productId={}", product.getId());
                }

                // 清除Redis缓存
                redisTemplate.delete(List.of(priceKey, winnerKey));
            }
        } finally {
            redisTemplate.delete(LOCK_KEY);
        }
    }
}
