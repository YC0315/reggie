package com.itheima.reggie.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.domain.Setmeal;
import com.itheima.reggie.domain.SetmealDish;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    // 新增套餐，操作两张表
    @Override
    @Transactional  // 操作两张表一定记得开启事务管理
    public void saveWithDish(SetmealDto setmealDto) {
        // 保存套餐的基本信息，操作setmeal表
        //INSERT INTO setmeal ( id, category_id, name, price, status, code, description, image, create_time, update_time, create_user, update_user ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();  // 拿出setmealDto里面的setmealdish集合
        //遍历每一个setmealDish，设定id值
        setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        //保存套餐和菜品的关联关系，操作setmeal_dish表
        setmealDishService.saveBatch(setmealDishes);
        //INSERT INTO setmeal_dish ( id, setmeal_id, dish_id, name, price, copies, create_time, update_time, create_user, update_user ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )

    }

    // 删除套餐同时也要删除套餐关系表中所关联的菜品，因此需要操作两张表
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //查询套餐的状态，看是否可以删除，正在售卖的不可删除
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();  // QueryWrapper其实可以理解成一个放查询条件的盒子，我们把查询条件放在里面，他就会自动按照对应的条件进行查询数据。
        //设定查询条件
        //  SELECT COUNT( * ) FROM setmeal WHERE (id IN (?) AND status = ?)
        lqw.in(Setmeal::getId, ids);
        lqw.eq(Setmeal::getStatus, 1);
        int count = this.count(lqw);

        // 如果正在售卖，则抛出异常
        if(count > 0){
            //抛出异常
            throw new CustomException("正在售卖，无法删除！");
        }

        // 删除套餐表中的套餐数据
        this.removeByIds(ids);

        // 删除关系表中的数据
        // 在关系表中查询哪套餐id被删除了
        // delete from setmeal_dish where setmeal_id in (ids)
        LambdaQueryWrapper<SetmealDish> lqw1 = new LambdaQueryWrapper<>();
        //设定筛选条件
        lqw1.in(SetmealDish::getSetmealId, ids);

        // 删除关系表中对应的菜品
        setmealDishService.remove(lqw1);




    }
}
