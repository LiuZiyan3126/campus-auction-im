package com.ca.module.user.controller;

import com.ca.common.util.ResultVO;
import com.ca.module.user.dto.LoginDTO;
import com.ca.module.user.dto.RegisterDTO;
import com.ca.module.user.service.UserService;
import com.ca.module.user.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResultVO<String> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return ResultVO.ok("注册成功，请等待管理员审核");
    }

    @PostMapping("/login")
    public ResultVO<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return ResultVO.ok("登录成功", userService.login(dto));
    }

    @PostMapping("/refresh")
    public ResultVO<String> refresh(@RequestBody String refreshToken) {
        return ResultVO.ok(userService.refreshToken(refreshToken));
    }
}
