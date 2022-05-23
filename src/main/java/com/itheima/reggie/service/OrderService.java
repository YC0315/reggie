package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.domain.Orders;

public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);

    //服务端查看用户订单
    public Page lists(int page, int pageSize, String number, String beginTime, String endTime);

    //用户再来一单
    public void againSubmit(Long id);

    //移动端查看用户订单
    public Page catOrderDetild(int page, int pageSize);
}
