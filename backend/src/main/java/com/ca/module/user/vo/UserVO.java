package com.ca.module.user.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserVO {
    private Long id;
    private String studentId;
    private String username;
    private String avatar;
    private String email;
    private String phone;
    private String role;
    private Integer reputationScore;
    private String status;
    private LocalDateTime createdAt;
}
