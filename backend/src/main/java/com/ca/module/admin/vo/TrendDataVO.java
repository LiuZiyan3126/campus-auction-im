package com.ca.module.admin.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrendDataVO {
    private String date;
    private long newUsers;
    private long newOrders;
    private double totalAmount;
}
