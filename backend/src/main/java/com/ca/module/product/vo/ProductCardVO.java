package com.ca.module.product.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductCardVO {
    private Long id;
    private String title;
    private String type;
    private BigDecimal fixedPrice;
    private BigDecimal startPrice;
    private BigDecimal currentPrice;
    private String coverImage;
    private Integer viewCount;
    private String status;
    private String sellerName;
    private String sellerAvatar;
    private LocalDateTime createdAt;
    private LocalDateTime auctionEnd;
}
