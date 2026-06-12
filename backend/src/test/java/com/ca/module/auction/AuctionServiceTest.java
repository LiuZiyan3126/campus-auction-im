package com.ca.module.auction;

import com.ca.common.exception.BusinessException;
import com.ca.module.auction.entity.Bid;
import com.ca.module.auction.mapper.BidMapper;
import com.ca.module.auction.service.impl.AuctionServiceImpl;
import com.ca.module.product.entity.Product;
import com.ca.module.product.mapper.ProductMapper;
import com.ca.module.user.entity.User;
import com.ca.module.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.ca.module.order.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock StringRedisTemplate redisTemplate;
    @Mock BidMapper bidMapper;
    @Mock ProductMapper productMapper;
    @Mock UserMapper userMapper;
    @Mock SimpMessagingTemplate messagingTemplate;
    @Mock OrderService orderService;
    @Mock ValueOperations<String, String> valueOps;
    @InjectMocks AuctionServiceImpl auctionService;

    // ===== 出价校验（Service层校验在Lua执行前） =====

    @Test
    void testPlaceBid_ProductNotFound_ShouldThrowException() {
        when(productMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> auctionService.placeBid(1L, 999L, new BigDecimal("100")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("商品不存在");
    }

    @Test
    void testPlaceBid_NotAuctionType_ShouldThrowException() {
        Product product = new Product();
        product.setId(1L);
        product.setType("SALE");
        when(productMapper.selectById(1L)).thenReturn(product);

        assertThatThrownBy(() -> auctionService.placeBid(1L, 1L, new BigDecimal("100")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不是拍卖商品");
    }

    @Test
    void testPlaceBid_SelfBid_ShouldThrowException() {
        Product product = new Product();
        product.setId(1L);
        product.setType("AUCTION");
        product.setStatus("ACTIVE");
        product.setSellerId(1L);
        product.setAuctionEnd(LocalDateTime.now().plusHours(1));
        when(productMapper.selectById(1L)).thenReturn(product);

        assertThatThrownBy(() -> auctionService.placeBid(1L, 1L, new BigDecimal("100")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能给自己的商品出价");
    }

    @Test
    void testPlaceBid_AuctionEnded_ShouldThrowException() {
        Product product = new Product();
        product.setId(1L);
        product.setType("AUCTION");
        product.setStatus("ACTIVE");
        product.setSellerId(2L);
        product.setAuctionEnd(LocalDateTime.now().minusHours(1));
        when(productMapper.selectById(1L)).thenReturn(product);

        assertThatThrownBy(() -> auctionService.placeBid(1L, 1L, new BigDecimal("100")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("拍卖已结束");
    }

    // ===== Redis Lua脚本测试（模拟成功/失败） =====

    @Test
    void testPlaceBid_LuaScriptSuccess_ShouldPushWebSocket() {
        Product product = new Product();
        product.setId(1L);
        product.setType("AUCTION");
        product.setStatus("ACTIVE");
        product.setSellerId(2L);
        product.setStartPrice(new BigDecimal("10"));
        product.setAuctionEnd(LocalDateTime.now().plusHours(1));
        when(productMapper.selectById(1L)).thenReturn(product);

        User bidder = new User();
        bidder.setId(1L);
        bidder.setUsername("测试用户");
        when(userMapper.selectById(1L)).thenReturn(bidder);

        // 模拟Lua返回成功
        List<Object> luaResult = List.of(1L, "85.00");
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any(), any(), any()))
                .thenReturn(luaResult);

        var result = auctionService.placeBid(1L, 1L, new BigDecimal("85"));

        assertThat(result.getBidAmount()).isEqualByComparingTo(new BigDecimal("85"));
        verify(messagingTemplate).convertAndSend(eq("/topic/auction/1"), anyMap());
    }

    @Test
    void testPlaceBid_LuaScriptReject_ShouldThrowException() {
        Product product = new Product();
        product.setId(1L);
        product.setType("AUCTION");
        product.setStatus("ACTIVE");
        product.setSellerId(2L);
        product.setStartPrice(new BigDecimal("100"));
        product.setAuctionEnd(LocalDateTime.now().plusHours(1));
        when(productMapper.selectById(1L)).thenReturn(product);

        when(userMapper.selectById(1L)).thenReturn(new User());

        // 模拟Lua返回失败（出价太低）
        List<Object> luaResult = List.of(0L, "出价必须高于当前价 100.00");
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any(), any(), any()))
                .thenReturn(luaResult);

        assertThatThrownBy(() -> auctionService.placeBid(1L, 1L, new BigDecimal("50")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("出价必须高于");
    }

    // ===== 出价记录测试 =====

    @Test
    void testGetBidHistory_ShouldReturnPage() {
        when(bidMapper.selectPage(any(), any())).thenReturn(mock(com.baomidou.mybatisplus.core.metadata.IPage.class));

        var result = auctionService.getBidHistory(1L, 1, 20);

        assertThat(result).isNotNull();
    }

    // ===== 当前价测试 =====

    @Test
    void testGetCurrentPrice_RedisHasValue_ShouldReturnRedisValue() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("auction:1:price")).thenReturn("88.00");

        var result = auctionService.getCurrentPrice(1L);

        assertThat(result).isEqualByComparingTo(new BigDecimal("88.00"));
    }
}
