package com.ca.module.auction.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BidVO {
    private Long id;
    private Long productId;
    private Long bidderId;
    private String bidderName;
    private BigDecimal bidAmount;
    private LocalDateTime bidTime;
}
