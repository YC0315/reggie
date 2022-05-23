package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.domain.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据:{}",shoppingCart);

        //设置用户id，指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);

        if(dishId != null){
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);

        }else{
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //查询当前菜品或者套餐是否在购物车中
        //SQL:select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if(cartServiceOne != null){
            //如果已经存在，就在原来数量基础上加一
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        }else{
            //如果不存在，则添加到购物车，数量默认就是一
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }

        return Result.success(cartServiceOne);
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(){
        log.info("查看购物车...");

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return Result.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public Result<String> clean(){

        shoppingCartService.clean();

        return Result.success("清空购物车成功");
    }

    // 手机端减少购物车中的菜品或套餐的数量，前端传过来的是菜品和订单的id
    @PostMapping("/sub")
    public Result<String> dropNumber(@RequestBody ShoppingCart shoppingCart) {

        Long dishId = shoppingCart.getDishId();
        //设置条件构造器
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        //如果减少的是菜品的数量
        if (dishId != null) {
            lqw.eq(ShoppingCart::getDishId, dishId);
            //getOne方法最终得到的是 实体类对象
            ShoppingCart shoppingCart1 = shoppingCartService.getOne(lqw);
            shoppingCart1.setNumber(shoppingCart1.getNumber() - 1); // 减少购物车菜品的数量
            shoppingCartService.updateById(shoppingCart1);
            return Result.success("操作成功！");
        }
        Long setmealId = shoppingCart.getSetmealId();
        if (setmealId != null) {
            lqw.eq(ShoppingCart::getSetmealId, setmealId);
            ShoppingCart shoppingCart2 = shoppingCartService.getOne(lqw);
            shoppingCart2.setNumber(shoppingCart2.getNumber() - 1);
            shoppingCartService.updateById(shoppingCart2);
            return Result.success("操作成功！");
        }
        return Result.error("操作失败！");
    }

}