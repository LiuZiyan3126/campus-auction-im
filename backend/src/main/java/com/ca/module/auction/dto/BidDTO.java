package com.ca.module.auction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BidDTO {
    @NotNull
    @Positive(message = "出价必须大于0")
    private BigDecimal bidAmount;
}
