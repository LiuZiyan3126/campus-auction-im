package com.ca.module.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ca.module.order.vo.OrderVO;

public interface OrderService {
    OrderVO createOrder(Long buyerId, Long productId);
    OrderVO createOrderFromAuction(Long productId, Long winnerId, Long sellerId, java.math.BigDecimal amount);
    OrderVO pay(Long userId, Long orderId);
    OrderVO ship(Long userId, Long orderId);
    OrderVO confirm(Long userId, Long orderId);
    OrderVO cancel(Long userId, Long orderId);
    IPage<OrderVO> listOrders(Long userId, String role, String status, int page, int size);
    OrderVO getById(Long userId, Long orderId);
}
