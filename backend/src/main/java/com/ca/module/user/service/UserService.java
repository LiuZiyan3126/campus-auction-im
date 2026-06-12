package com.ca.module.user.service;

import com.ca.module.user.dto.LoginDTO;
import com.ca.module.user.dto.RegisterDTO;
import com.ca.module.user.dto.UpdateUserDTO;
import com.ca.module.user.vo.LoginVO;
import com.ca.module.user.vo.UserProfileVO;
import com.ca.module.user.vo.UserVO;

public interface UserService {
    void register(RegisterDTO dto);
    LoginVO login(LoginDTO dto);
    String refreshToken(String refreshToken);
    UserVO getMyProfile(Long userId);
    UserVO updateMyProfile(Long userId, UpdateUserDTO dto);
    UserProfileVO getUserProfile(Long userId);
}
