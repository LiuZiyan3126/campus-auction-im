package com.ca.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("products")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sellerId;
    private String title;
    private String description;
    private Integer categoryId;
    private String type;  // SALE / AUCTION
    private BigDecimal startPrice;
    private BigDecimal fixedPrice;
    private BigDecimal currentPrice;
    private String status;  // PENDING / ACTIVE / SOLD / EXPIRED / BANNED
    private LocalDateTime auctionStart;
    private LocalDateTime auctionEnd;
    private Long winnerId;
    private Integer viewCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private LocalDateTime deletedAt;
}
