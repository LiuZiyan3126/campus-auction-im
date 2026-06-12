package com.ca.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BanDTO {
    @NotBlank
    private String action;  // BAN / UNBAN
}
