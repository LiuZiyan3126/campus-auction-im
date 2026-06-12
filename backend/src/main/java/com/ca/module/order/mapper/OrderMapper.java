package com.ca.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ca.module.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
