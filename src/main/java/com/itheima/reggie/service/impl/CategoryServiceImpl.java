package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.domain.Category;
import com.itheima.reggie.domain.Dish;
import com.itheima.reggie.domain.Setmeal;
import com.itheima.reggie.mapper.CategoryMappper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service  // 生成bean对象放在容器里
// ServiceImpl<CategoryMappper, Category> ，CategoryMappper是mapper层的接口，因为要自动注入，所以要有。Category是Service层方法的返回值类型
// 这样就不用写成员变量自动注入以及service中的方法了！！
public class CategoryServiceImpl extends ServiceImpl<CategoryMappper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    // 根据id删除分类，删除之前先判断菜品分类或套餐分类是否关联菜品或套餐
    @Override
    public void remove(Long ids) {

        // 构造一个查询条件
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件,根据category_id进行查询
        lambdaQueryWrapper.eq(Dish::getCategoryId, ids);
        // 统计关联菜品的数量，大于0则该菜品分类不能删除
        int count = dishService.count(lambdaQueryWrapper);

        // 查询当前菜品分类是否关联了菜品，如果关联，抛出异常
        if(count != 0){
            throw new CustomException("该菜品分类已经关联了菜品，无法删除！");
        }


        // 构造一个查询条件
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        // 设置查询条件,根据category_id进行查询
        lambdaQueryWrapper1.eq(Setmeal::getCategoryId, ids);
        // 统计关联菜品的数量，大于0则该菜品分类不能删除
        int count1 = setmealService.count(lambdaQueryWrapper1);

        // 查询当前菜品分类是否关联了菜品，如果关联，抛出异常
        if(count1 != 0){
            // ，已经关联套餐，抛出异常
            throw new CustomException("该套餐分类已经关联了套餐，无法删除！");
        }

        // 没有关联菜品或套餐，直接删除,super是IService接口
        super.removeById(ids);

    }
}
