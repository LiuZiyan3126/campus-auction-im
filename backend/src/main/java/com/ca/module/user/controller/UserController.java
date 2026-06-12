package com.ca.module.user.controller;

import com.ca.common.util.ResultVO;
import com.ca.module.user.dto.UpdateUserDTO;
import com.ca.module.user.service.UserService;
import com.ca.module.user.vo.UserProfileVO;
import com.ca.module.user.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResultVO<UserVO> getMyProfile(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResultVO.ok(userService.getMyProfile(userId));
    }

    @PutMapping("/me")
    public ResultVO<UserVO> updateMyProfile(Authentication auth, @Valid @RequestBody UpdateUserDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return ResultVO.ok(userService.updateMyProfile(userId, dto));
    }

    @GetMapping("/{id}")
    public ResultVO<UserProfileVO> getUserProfile(@PathVariable Long id) {
        return ResultVO.ok(userService.getUserProfile(id));
    }
}
