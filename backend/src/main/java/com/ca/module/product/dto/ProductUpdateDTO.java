package com.ca.module.product.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateDTO {
    @Size(max = 200)
    private String title;
    private String description;
    private Integer categoryId;
    private BigDecimal startPrice;
    private BigDecimal fixedPrice;
}
