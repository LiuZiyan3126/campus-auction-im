package com.ca.module.product.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProductVO {
    private Long id;
    private Long sellerId;
    private String sellerName;
    private String sellerAvatar;
    private String title;
    private String description;
    private Integer categoryId;
    private String categoryName;
    private String type;
    private BigDecimal startPrice;
    private BigDecimal fixedPrice;
    private BigDecimal currentPrice;
    private String status;
    private LocalDateTime auctionStart;
    private LocalDateTime auctionEnd;
    private Long winnerId;
    private String winnerName;
    private Integer viewCount;
    private List<String> images;
    private LocalDateTime createdAt;
}
