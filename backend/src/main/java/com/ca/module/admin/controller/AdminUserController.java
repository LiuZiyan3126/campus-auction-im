package com.ca.module.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ca.common.util.PageVO;
import com.ca.common.util.ResultVO;
import com.ca.module.admin.dto.BanDTO;
import com.ca.module.admin.dto.VerifyDTO;
import com.ca.module.admin.service.AdminService;
import com.ca.module.user.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;

    @GetMapping
    public ResultVO<PageVO<UserVO>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        IPage<UserVO> result = adminService.listUsers(page, size, status, keyword);
        return ResultVO.ok(PageVO.of(result));
    }

    @PutMapping("/{id}/verify")
    public ResultVO<Void> verifyUser(@PathVariable Long id, @Valid @RequestBody VerifyDTO dto) {
        adminService.verifyUser(id, dto.getAction());
        return ResultVO.ok(null);
    }

    @PutMapping("/{id}/ban")
    public ResultVO<Void> banUser(@PathVariable Long id, @Valid @RequestBody BanDTO dto) {
        adminService.banUser(id, dto.getAction());
        return ResultVO.ok(null);
    }
}
