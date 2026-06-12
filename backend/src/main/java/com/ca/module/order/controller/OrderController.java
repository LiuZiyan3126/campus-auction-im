package com.ca.module.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ca.common.util.PageVO;
import com.ca.common.util.ResultVO;
import com.ca.module.order.dto.OrderCreateDTO;
import com.ca.module.order.service.OrderService;
import com.ca.module.order.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResultVO<PageVO<OrderVO>> list(Authentication auth,
                                           @RequestParam(required = false) String role,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) auth.getPrincipal();
        IPage<OrderVO> result = orderService.listOrders(userId, role, status, page, size);
        return ResultVO.ok(PageVO.of(result));
    }

    @PostMapping
    public ResultVO<OrderVO> create(Authentication auth, @Valid @RequestBody OrderCreateDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return ResultVO.ok(orderService.createOrder(userId, dto.getProductId()));
    }

    @GetMapping("/{id}")
    public ResultVO<OrderVO> getById(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        return ResultVO.ok(orderService.getById(userId, id));
    }

    @PostMapping("/{id}/pay")
    public ResultVO<OrderVO> pay(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        return ResultVO.ok(orderService.pay(userId, id));
    }

    @PostMapping("/{id}/ship")
    public ResultVO<OrderVO> ship(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        return ResultVO.ok(orderService.ship(userId, id));
    }

    @PostMapping("/{id}/confirm")
    public ResultVO<OrderVO> confirm(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        return ResultVO.ok(orderService.confirm(userId, id));
    }

    @PostMapping("/{id}/cancel")
    public ResultVO<OrderVO> cancel(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        return ResultVO.ok(orderService.cancel(userId, id));
    }
}
