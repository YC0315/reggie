package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.domain.*;
import com.itheima.reggie.dto.OrderDto;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {


    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     */
    @Transactional
    public void submit(Orders orders) {
        //获得当前用户id,用户登录时已经将员工id存入Session中了
        Long userId = BaseContext.getCurrentId();
        //Long userId = (Long) session.getAttribute("user");

        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(wrapper);

        if(shoppingCarts == null || shoppingCarts.size() == 0){
            throw new CustomException("购物车为空，不能下单");
        }

        //查询用户数据
        User user = userService.getById(userId);

        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if(addressBook == null){
            throw new CustomException("用户地址信息有误，不能下单");
        }

        long orderId = IdWorker.getId();//订单号

        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(BaseContext.getCurrentId());
        orders.setNumber(String.valueOf(orderId));
        //orders.setUserName(user.getName());  // 没有username
        orders.setUserName(user.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        //向订单表插入数据，一条数据
        this.save(orders);

        //向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(wrapper);
    }



    // 订单配送完成后再来一单功能,传过来的参数是订单的id
    /*
     * 前端点击再来一单是直接跳转到购物车的，所以为了避免数据有问题，再跳转之前我们需要把购物车的数据给清除
     * ①通过orderId获取订单明细
     * ②把订单明细的数据的数据塞到购物车表中，不过在此之前要先把购物车表中的数据给清除(清除的是当前登录用户的购物车表中的数据)，
     * 不然就会导致再来一单的数据有问题；
     * (这样可能会影响用户体验，但是对于外卖来说，用户体验的影响不是很大，电商项目就不能这么干了)
     */
    @Override
    @Transactional
    public void againSubmit(Long id) {
        //根据订单id查询有哪些菜品
        //设置条件构造器
        LambdaQueryWrapper<OrderDetail> lqw = new LambdaQueryWrapper<>();
        lqw.eq(OrderDetail::getOrderId, id);
        List<OrderDetail> orderDetails = orderDetailService.list(lqw); // 将所有的菜品查出来放到列表中
        // 清空购物车
        shoppingCartService.clean();

        // 获取当前用户的id
        Long uderID = BaseContext.getCurrentId();
        //Long uderID = (Long) session.getAttribute("user");

        List<ShoppingCart> shoppingCarts = orderDetails.stream().map((item)->{
            //将订单表和订单明细表中的数据赋值给购物车对象
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setName(item.getName());
            shoppingCart.setUserId(uderID);
            if(item.getDishId() != null){
                shoppingCart.setDishId(item.getDishId());
            }else{
                shoppingCart.setSetmealId(item.getSetmealId());
            }
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setImage(item.getImage());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        //添加购物车数据实体到购物车
        shoppingCartService.saveBatch(shoppingCarts);
    }

    // 服务端查看用户订单
    @Override
    public Page lists(int page, int pageSize, String number, String beginTime, String endTime) {
        // 设置分页构造器对象
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        // 设置条件构造器
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        //设置筛选条件
        lqw.like(number!= null,Orders::getNumber, number);
        //设置查询时间
        lqw.gt(StringUtils.isNotEmpty(beginTime),Orders::getOrderTime, beginTime);
        lqw.lt(StringUtils.isNotEmpty(endTime),Orders::getOrderTime, endTime);
        //执行查询
        this.page(ordersPage, lqw);
        return ordersPage;
    }


    //抽离的一个方法，通过订单id查询订单明细，得到一个订单明细的集合
    //这里抽离出来是为了避免在stream中遍历的时候直接使用构造条件来查询导致eq叠加，从而导致后面查询的数据都是null
    public List<OrderDetail> getOrderDetailListByOrderId(Long orderId){
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, orderId);
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);
        return orderDetailList;
    }
    // 移动端查看用户订单
    @Override
    public Page catOrderDetild(int page, int pageSize) {
        // 设置分页构造器
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        Page<OrderDto> orderDtoPage = new Page<>();

        //设置条件构造器
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        //设置查询条件,查询当前用户的订单，其他用户的订单不应该显示在这setmeal
        lqw.eq(Orders::getUserId, BaseContext.getCurrentId());
        //设置排序条件
        lqw.orderByDesc(Orders::getOrderTime);
        this.page(ordersPage, lqw);

        //通过orderId查询对应的orderDetil
        LambdaQueryWrapper<OrderDetail> lqw2 = new LambdaQueryWrapper<>();

        // 将分页构造器对象ordersPage的属性拷贝到orderDtoPage对象上，药重新设置records
        BeanUtils.copyProperties(ordersPage, orderDtoPage, "records");

        List<Orders> records = ordersPage.getRecords();
        List<OrderDto> orderDtos = records.stream().map((item)->{
            OrderDto orderDto = new OrderDto();
            // 获取订单的id
            Long id = item.getId();
            //根据订单id获取订单订单详细表中的数据
            List<OrderDetail> orderDetailListByOrderId = this.getOrderDetailListByOrderId(id);
            // 将Order实体的属性赋值给orderDto实体
            BeanUtils.copyProperties(item, orderDto);
            orderDto.setOrderDetails(orderDetailListByOrderId);
            return orderDto;
        }).collect(Collectors.toList());

        orderDtoPage.setRecords(orderDtos);
        return orderDtoPage;
    }
}