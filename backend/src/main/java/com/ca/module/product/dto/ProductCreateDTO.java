package com.ca.module.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateDTO {
    @NotBlank @Size(max = 200)
    private String title;
    private String description;
    @NotNull
    private Integer categoryId;
    @NotBlank
    private String type;  // SALE / AUCTION
    private BigDecimal startPrice;
    private BigDecimal fixedPrice;
    private Integer auctionDuration;  // 拍卖时长（小时），仅AUCTION时使用
}
