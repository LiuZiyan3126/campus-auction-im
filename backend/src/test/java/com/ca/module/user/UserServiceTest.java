package com.ca.module.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ca.common.exception.BusinessException;
import com.ca.common.security.JwtTokenProvider;
import com.ca.module.user.dto.LoginDTO;
import com.ca.module.user.dto.RegisterDTO;
import com.ca.module.user.dto.UpdateUserDTO;
import com.ca.module.user.entity.User;
import com.ca.module.user.mapper.UserMapper;
import com.ca.module.user.service.impl.UserServiceImpl;
import com.ca.module.user.vo.UserProfileVO;
import com.ca.module.user.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserMapper userMapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider jwtTokenProvider;
    @InjectMocks UserServiceImpl userService;

    private RegisterDTO registerDTO;
    private LoginDTO loginDTO;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerDTO = new RegisterDTO();
        registerDTO.setStudentId("20210001");
        registerDTO.setEmail("test@example.com");
        registerDTO.setPassword("abc12345");
        registerDTO.setPhone("13800138000");

        loginDTO = new LoginDTO();
        loginDTO.setStudentId("20210001");
        loginDTO.setPassword("abc12345");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setStudentId("20210001");
        mockUser.setUsername("20210001");
        mockUser.setPasswordHash("hashed_password");
        mockUser.setEmail("test@example.com");
        mockUser.setRole("USER");
        mockUser.setReputationScore(100);
        mockUser.setStatus("ACTIVE");
    }

    // ===== 注册测试 =====

    @Test
    void testRegister_NewUser_ShouldSucceed() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(passwordEncoder.encode("abc12345")).thenReturn("hashed_password");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        userService.register(registerDTO);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getStudentId()).isEqualTo("20210001");
        assertThat(saved.getStatus()).isEqualTo("PENDING");
        assertThat(saved.getRole()).isEqualTo("USER");
        assertThat(saved.getReputationScore()).isEqualTo(100);
    }

    @Test
    void testRegister_DuplicateStudentId_ShouldThrowException() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThatThrownBy(() -> userService.register(registerDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已被注册");
    }

    // ===== 登录测试 =====

    @Test
    void testLogin_ValidCredentials_ShouldReturnToken() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);
        when(passwordEncoder.matches("abc12345", "hashed_password")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(1L, "USER")).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(1L)).thenReturn("refresh-token");

        var result = userService.login(loginDTO);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getUserInfo().getId()).isEqualTo(1L);
    }

    @Test
    void testLogin_WrongPassword_ShouldThrowException() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);
        when(passwordEncoder.matches("abc12345", "hashed_password")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(loginDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void testLogin_BannedUser_ShouldThrowException() {
        mockUser.setStatus("BANNED");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.login(loginDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("封禁");
    }

    // ===== 个人资料测试 =====

    @Test
    void testGetMyProfile_ExistingUser_ShouldReturnUserVO() {
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        UserVO result = userService.getMyProfile(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStudentId()).isEqualTo("20210001");
        assertThat(result.getUsername()).isEqualTo("20210001");
    }

    @Test
    void testGetMyProfile_NonExistingUser_ShouldThrowException() {
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> userService.getMyProfile(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
    }

    @Test
    void testUpdateMyProfile_ShouldUpdateFields() {
        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setUsername("新昵称");
        dto.setPhone("13900139000");

        UserVO result = userService.updateMyProfile(1L, dto);

        assertThat(result.getUsername()).isEqualTo("新昵称");
        verify(userMapper).updateById(mockUser);
    }

    // ===== 用户主页测试 =====

    @Test
    void testGetUserProfile_ShouldReturnProfileWithStats() {
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        UserProfileVO result = userService.getUserProfile(1L);

        assertThat(result.getUserInfo()).isNotNull();
        assertThat(result.getTradeCount()).isZero();
    }
}
