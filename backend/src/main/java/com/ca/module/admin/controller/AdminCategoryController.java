package com.ca.module.admin.controller;

import com.ca.common.util.ResultVO;
import com.ca.module.admin.service.AdminService;
import com.ca.module.product.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminService adminService;

    @GetMapping
    public ResultVO<List<Category>> listAll() {
        return ResultVO.ok(adminService.listCategories());
    }

    @PostMapping
    public ResultVO<Category> create(@RequestBody Category category) {
        return ResultVO.ok(adminService.createCategory(category));
    }

    @PutMapping("/{id}")
    public ResultVO<Category> update(@PathVariable Integer id, @RequestBody Category category) {
        return ResultVO.ok(adminService.updateCategory(id, category));
    }

    @DeleteMapping("/{id}")
    public ResultVO<Void> delete(@PathVariable Integer id) {
        adminService.deleteCategory(id);
        return ResultVO.ok(null);
    }
}
