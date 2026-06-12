package com.ca.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuditDTO {
    @NotBlank
    private String action;  // APPROVE / REJECT (下架=BAN)
}
