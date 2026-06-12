package com.ca.module.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ca.common.util.PageVO;
import com.ca.common.util.ResultVO;
import com.ca.module.admin.dto.AuditDTO;
import com.ca.module.admin.service.AdminService;
import com.ca.module.product.vo.ProductVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminService adminService;

    @GetMapping
    public ResultVO<PageVO<ProductVO>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        IPage<ProductVO> result = adminService.listProducts(page, size, status);
        return ResultVO.ok(PageVO.of(result));
    }

    @PutMapping("/{id}/audit")
    public ResultVO<Void> auditProduct(@PathVariable Long id, @Valid @RequestBody AuditDTO dto) {
        adminService.auditProduct(id, dto.getAction());
        return ResultVO.ok(null);
    }
}
