package com.itheima.reggie.dto;

import com.itheima.reggie.domain.OrderDetail;
import com.itheima.reggie.domain.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrderDto extends Orders {
    private List<OrderDetail> orderDetails;
}
