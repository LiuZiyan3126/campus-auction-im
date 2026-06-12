package com.ca.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ca.common.exception.BusinessException;
import com.ca.module.order.entity.Order;
import com.ca.module.order.mapper.OrderMapper;
import com.ca.module.order.service.OrderService;
import com.ca.module.order.vo.OrderVO;
import com.ca.module.product.entity.Product;
import com.ca.module.product.entity.ProductImage;
import com.ca.module.product.mapper.ProductImageMapper;
import com.ca.module.product.mapper.ProductMapper;
import com.ca.module.user.entity.User;
import com.ca.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper imageMapper;
    private final UserMapper userMapper;

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            "PENDING", Set.of("PAID", "CANCELLED"),
            "PAID", Set.of("SHIPPED"),
            "SHIPPED", Set.of("COMPLETED")
    );

    @Override
    @Transactional
    public OrderVO createOrder(Long buyerId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw BusinessException.notFound("商品不存在");
        if (!"SALE".equals(product.getType())) throw BusinessException.badRequest("只能购买一口价商品");
        if (!"ACTIVE".equals(product.getStatus())) throw BusinessException.badRequest("商品已下架或已售出");
        if (product.getSellerId().equals(buyerId)) throw BusinessException.badRequest("不能购买自己的商品");

        Order order = new Order();
        order.setProductId(productId);
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setAmount(product.getFixedPrice());
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        orderMapper.insert(order);

        product.setStatus("SOLD");
        productMapper.updateById(product);

        log.info("订单创建: orderId={}, buyerId={}, amount={}", order.getId(), buyerId, order.getAmount());
        return buildOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO createOrderFromAuction(Long productId, Long winnerId, Long sellerId, BigDecimal amount) {
        Order order = new Order();
        order.setProductId(productId);
        order.setBuyerId(winnerId);
        order.setSellerId(sellerId);
        order.setAmount(amount);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        orderMapper.insert(order);
        log.info("拍卖成交订单: orderId={}, winnerId={}, amount={}", order.getId(), winnerId, amount);
        return buildOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO pay(Long userId, Long orderId) {
        Order order = getAndValidate(orderId, userId, "PENDING", true, "PAID");
        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("订单支付: orderId={}", orderId);
        return buildOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO ship(Long userId, Long orderId) {
        Order order = getAndValidate(orderId, userId, "PAID", false, "SHIPPED");
        if (!order.getSellerId().equals(userId)) throw BusinessException.forbidden("只有卖家可以发货");
        order.setStatus("SHIPPED");
        order.setShippedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("订单发货: orderId={}", orderId);
        return buildOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO confirm(Long userId, Long orderId) {
        Order order = getAndValidate(orderId, userId, "SHIPPED", true, "COMPLETED");
        order.setStatus("COMPLETED");
        order.setCompletedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("订单完成: orderId={}", orderId);
        return buildOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO cancel(Long userId, Long orderId) {
        Order order = getAndValidate(orderId, userId, "PENDING", true, "CANCELLED");
        order.setStatus("CANCELLED");
        orderMapper.updateById(order);

        // 恢复商品状态
        Product product = productMapper.selectById(order.getProductId());
        if (product != null && "SOLD".equals(product.getStatus())) {
            product.setStatus("ACTIVE");
            productMapper.updateById(product);
        }
        log.info("订单取消: orderId={}", orderId);
        return buildOrderVO(order);
    }

    @Override
    public IPage<OrderVO> listOrders(Long userId, String role, String status, int page, int size) {
        Page<Order> pager = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if ("buyer".equals(role)) {
            wrapper.eq(Order::getBuyerId, userId);
        } else if ("seller".equals(role)) {
            wrapper.eq(Order::getSellerId, userId);
        } else {
            wrapper.and(w -> w.eq(Order::getBuyerId, userId).or().eq(Order::getSellerId, userId));
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        IPage<Order> result = orderMapper.selectPage(pager, wrapper);
        return result.convert(this::buildOrderVO);
    }

    @Override
    public OrderVO getById(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw BusinessException.notFound("订单不存在");
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw BusinessException.forbidden("无权查看此订单");
        }
        return buildOrderVO(order);
    }

    private Order getAndValidate(Long orderId, Long userId, String requiredStatus, boolean isBuyer, String targetStatus) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw BusinessException.notFound("订单不存在");
        if (isBuyer && !order.getBuyerId().equals(userId)) throw BusinessException.forbidden("只有买家可以操作");
        if (!requiredStatus.equals(order.getStatus())) {
            throw BusinessException.badRequest("当前订单状态不允许此操作（" + order.getStatus() + "）");
        }
        Set<String> allowed = ALLOWED_TRANSITIONS.get(order.getStatus());
        if (allowed == null || !allowed.contains(targetStatus)) {
            throw BusinessException.badRequest("不允许从 " + order.getStatus() + " 变为 " + targetStatus);
        }
        return order;
    }

    private OrderVO buildOrderVO(Order order) {
        Product product = productMapper.selectById(order.getProductId());
        User buyer = userMapper.selectById(order.getBuyerId());
        User seller = userMapper.selectById(order.getSellerId());
        ProductImage cover = imageMapper.selectOne(new LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getProductId, order.getProductId())
                .eq(ProductImage::getSortOrder, 0));

        String statusLabel = switch (order.getStatus()) {
            case "PENDING" -> "待付款";
            case "PAID" -> "已付款";
            case "SHIPPED" -> "已发货";
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            default -> order.getStatus();
        };

        return OrderVO.builder()
                .id(order.getId())
                .productId(order.getProductId())
                .productTitle(product != null ? product.getTitle() : null)
                .productImage(cover != null ? cover.getImageUrl() : null)
                .buyerId(order.getBuyerId())
                .buyerName(buyer != null ? buyer.getUsername() : null)
                .sellerId(order.getSellerId())
                .sellerName(seller != null ? seller.getUsername() : null)
                .amount(order.getAmount())
                .status(order.getStatus())
                .statusLabel(statusLabel)
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .shippedAt(order.getShippedAt())
                .completedAt(order.getCompletedAt())
                .build();
    }
}
