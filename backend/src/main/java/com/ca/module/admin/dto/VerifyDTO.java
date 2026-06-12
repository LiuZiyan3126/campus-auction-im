package com.ca.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyDTO {
    @NotBlank
    private String action;  // APPROVE / REJECT
}
