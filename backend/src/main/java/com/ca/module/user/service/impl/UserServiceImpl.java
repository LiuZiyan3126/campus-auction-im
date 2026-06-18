package com.ca.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ca.common.exception.BusinessException;
import com.ca.common.security.JwtTokenProvider;
import com.ca.module.user.dto.LoginDTO;
import com.ca.module.user.dto.RegisterDTO;
import com.ca.module.user.dto.UpdateUserDTO;
import com.ca.module.user.entity.User;
import com.ca.module.user.mapper.UserMapper;
import com.ca.module.user.service.UserService;
import com.ca.module.user.vo.LoginVO;
import com.ca.module.user.vo.UserProfileVO;
import com.ca.module.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getStudentId, dto.getStudentId())) > 0) {
            throw BusinessException.badRequest("该学号已被注册");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, dto.getEmail())) > 0) {
            throw BusinessException.badRequest("该邮箱已被注册");
        }
        User user = new User();
        user.setStudentId(dto.getStudentId());
        user.setUsername(dto.getStudentId()); // 默认昵称=学号
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole("USER");
        user.setReputationScore(100);
        user.setStatus("ACTIVE");
        userMapper.insert(user);
        log.info("用户注册成功: studentId={}", dto.getStudentId());
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getStudentId, dto.getStudentId()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }
        if ("BANNED".equals(user.getStatus())) {
            throw BusinessException.forbidden("账号已被封禁");
        }
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        log.info("用户登录成功: userId={}", user.getId());
        return LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userInfo(toUserVO(user))
                .build();
    }

    @Override
    public String refreshToken(String refreshToken) {
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw BusinessException.unauthorized("无效的RefreshToken");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.unauthorized("用户不存在");
        }
        return jwtTokenProvider.generateAccessToken(userId, user.getRole());
    }

    @Override
    public UserVO getMyProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw BusinessException.notFound("用户不存在");
        return toUserVO(user);
    }

    @Override
    @Transactional
    public UserVO updateMyProfile(Long userId, UpdateUserDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) throw BusinessException.notFound("用户不存在");
        if (dto.getUsername() != null) user.setUsername(dto.getUsername());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        userMapper.updateById(user);
        return toUserVO(user);
    }

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw BusinessException.notFound("用户不存在");
        return UserProfileVO.builder()
                .userInfo(toUserVO(user))
                .tradeCount(0L)
                .goodRate(100.0)
                .recentReviews(Collections.emptyList())
                .build();
    }

    private UserVO toUserVO(User user) {
        return UserVO.builder()
                .id(user.getId())
                .studentId(user.getStudentId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .reputationScore(user.getReputationScore())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
