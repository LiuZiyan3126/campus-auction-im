package com.ca.module.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ca.common.exception.BusinessException;
import com.ca.module.order.entity.Order;
import com.ca.module.order.mapper.OrderMapper;
import com.ca.module.order.service.impl.OrderServiceImpl;
import com.ca.module.product.entity.Product;
import com.ca.module.product.mapper.ProductImageMapper;
import com.ca.module.product.mapper.ProductMapper;
import com.ca.module.user.entity.User;
import com.ca.module.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderMapper orderMapper;
    @Mock ProductMapper productMapper;
    @Mock ProductImageMapper imageMapper;
    @Mock UserMapper userMapper;
    @InjectMocks OrderServiceImpl orderService;

    private Product mockProduct;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setSellerId(2L);
        mockProduct.setType("SALE");
        mockProduct.setFixedPrice(new BigDecimal("100"));
        mockProduct.setStatus("ACTIVE");

        mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setProductId(1L);
        mockOrder.setBuyerId(1L);
        mockOrder.setSellerId(2L);
        mockOrder.setAmount(new BigDecimal("100"));
        mockOrder.setStatus("PENDING");
    }

    // ===== 创建订单测试 =====

    @Test
    void testCreateOrder_Valid_ShouldSucceed() {
        when(productMapper.selectById(1L)).thenReturn(mockProduct);
        when(orderMapper.insert(any(Order.class))).thenAnswer(inv -> { inv.getArgument(0, Order.class).setId(1L); return 1; });

        var result = orderService.createOrder(1L, 1L);

        verify(orderMapper).insert(any(Order.class));
        verify(productMapper).updateById(any(Product.class));
        assertThat(result).isNotNull();
    }

    @Test
    void testCreateOrder_BuyOwnProduct_ShouldThrowException() {
        when(productMapper.selectById(1L)).thenReturn(mockProduct);

        assertThatThrownBy(() -> orderService.createOrder(2L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能购买自己的商品");
    }

    // ===== 支付与状态机测试 =====

    @Test
    void testPay_Valid_ShouldSucceed() {
        when(orderMapper.selectById(1L)).thenReturn(mockOrder);
        when(userMapper.selectById(anyLong())).thenReturn(new User());
        mockProduct.setStatus("SOLD");
        when(productMapper.selectById(1L)).thenReturn(mockProduct);

        var result = orderService.pay(1L, 1L);

        assertThat(result.getStatus()).isEqualTo("PAID");
    }

    @Test
    void testPay_NotBuyer_ShouldThrowException() {
        when(orderMapper.selectById(1L)).thenReturn(mockOrder);

        assertThatThrownBy(() -> orderService.pay(3L, 1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void testPay_AlreadyPaid_ShouldThrowException() {
        mockOrder.setStatus("PAID");
        when(orderMapper.selectById(1L)).thenReturn(mockOrder);

        assertThatThrownBy(() -> orderService.pay(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("当前订单状态不允许");
    }

    @Test
    void testShip_NotSeller_ShouldThrowException() {
        mockOrder.setStatus("PAID");
        when(orderMapper.selectById(1L)).thenReturn(mockOrder);

        assertThatThrownBy(() -> orderService.ship(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只有卖家可以发货");
    }

    @Test
    void testShip_Valid_ShouldSucceed() {
        mockOrder.setStatus("PAID");
        when(orderMapper.selectById(1L)).thenReturn(mockOrder);
        when(userMapper.selectById(anyLong())).thenReturn(new User());
        when(productMapper.selectById(1L)).thenReturn(mockProduct);

        var result = orderService.ship(2L, 1L);

        assertThat(result.getStatus()).isEqualTo("SHIPPED");
    }

    // ===== 取消订单测试 =====

    @Test
    void testCancel_RestoreProductStatus() {
        when(orderMapper.selectById(1L)).thenReturn(mockOrder);
        when(productMapper.selectById(1L)).thenReturn(mockProduct);

        var result = orderService.cancel(1L, 1L);

        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        verify(productMapper).updateById(mockProduct);
    }

    @Test
    void testCancel_NotPending_ShouldThrowException() {
        mockOrder.setStatus("PAID");
        when(orderMapper.selectById(1L)).thenReturn(mockOrder);

        assertThatThrownBy(() -> orderService.cancel(1L, 1L))
                .isInstanceOf(BusinessException.class);
    }
}
