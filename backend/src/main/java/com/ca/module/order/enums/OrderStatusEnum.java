package com.ca.module.order.enums;

public enum OrderStatusEnum {
    PENDING("待付款"),
    PAID("已付款"),
    SHIPPED("已发货"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String label;

    OrderStatusEnum(String label) { this.label = label; }
    public String getLabel() { return label; }
}
