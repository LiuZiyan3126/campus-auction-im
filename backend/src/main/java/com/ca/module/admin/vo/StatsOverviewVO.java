package com.ca.module.admin.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StatsOverviewVO {
    private long totalUsers;
    private long activeUsers;
    private long totalProducts;
    private long activeProducts;
    private long todayOrders;
    private BigDecimal todayAmount;
    private double auctionSuccessRate;
}
