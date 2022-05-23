package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.domain.Orders;
import com.itheima.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return Result.success("下单成功");
    }

    //服务端查看订单明细
    @GetMapping("/page")
    public Result<Page> list(int page, int pageSize, String number, String beginTime, String endTime){
        Page orderPage = orderService.lists(page, pageSize, number, beginTime, endTime);
        return Result.success(orderPage);
    }
    // 修改订单状态
    @PutMapping
    public Result<String> setStatus(@RequestBody Orders orders){

        Integer status = orders.getStatus();
        status += 1;
        orders.setStatus(status);
        orderService.updateById(orders);
        return Result.success("订单状态修改成功！");
    }

    //用户查看订单
    @GetMapping("/userPage")
    public Result<Page> orderlist(int page, int pageSize){

        Page pageDto = orderService.catOrderDetild(page, pageSize);
        return Result.success(pageDto);
    }

    /*
     * 订单配送完成后再来一单功能,传过来的参数是订单的id
     * 前端点击再来一单是直接跳转到购物车的，所以为了避免数据有问题，再跳转之前需要把购物车的数据给清除
     * ①通过orderId获取订单明细
     * ②把订单明细的数据的数据塞到购物车表中，不过在此之前要先把购物车表中的数据给清除(清除的是当前登录用户的购物车表中的数据)，
     */

    @PostMapping("/again")  //前端请求：Request URL: http://localhost:8080/order/again   要使用一个实体类去接收，然后取出里面的id值！！
    //请求参数带{}大括号里面的都是json数据，要使用@RequestBody
    public Result<String> addAgain(@RequestBody Orders orders){
        Long id = orders.getId();
        orderService.againSubmit(id);
        return Result.success("下单成功！");
    }

}