package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.domain.Dish;
import com.itheima.reggie.domain.DishFlavor;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service  // spring注入的是接口名，但是在容器里寻找的是接口的实现类new出来的bean对象
@Slf4j
public class DishSerrviceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;  // 用来操作口味表

    // 由于要操作两张表，因此这里要加入事务控制
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品的基本信息到菜品表
        this.save(dishDto);
        // 保存口味到口味表，口味参数是disdto的一部分
        Long dishid = dishDto.getId(); // 获得菜品id

        // 获得菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        //给每一个菜品填充id值
        for(DishFlavor dishFlavor:flavors){
            dishFlavor.setDishId(dishid);
        };

        /*flavors = flavors.stream().map((item) -> {
            item.setDishId(dishid);
            return item;
        }).collect(Collectors.toList());*/

        dishFlavorService.saveBatch(flavors);

    }
    //根据id查询菜品信息和对应的口味信息,查两张表
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        // 查询菜品信息，dish表
        Dish dish = this.getById(id);

        // 因为返回值类型是DishDto，因此创建一个对象，并且将Dish中的属性赋值给DishDto对象，然后再将flavor属性给DishDto对象加上去
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        // 查询菜品对应的口味信息，dish_falvor表
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId,dish.getId());  // 从口味表中查询菜品（id）对应的口味（id）
        List<DishFlavor> flavors = dishFlavorService.list(lqw);

        dishDto.setFlavors(flavors);


        return dishDto;
    }

    // 更新修改菜品信息以及口味信息
    @Override
    @Transactional  // 操作多张表要开启注解保证操作一致性
    public void updateWithFlavor(DishDto dishDto) {

        // 更新菜皮表信息
        this.updateById(dishDto);
        // 清理原有的口味表中的口味 delete from dish_falvor where dis_id = ???;
        //查询dish_id对应的口味
        //QueryWrapper其实可以理解成一个放查询条件的盒子，我们把查询条件放在里面，他就会自动按照对应的条件进行查询数据。
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(lqw);
        // 添加提交过来的口味信息-insert
        List<DishFlavor> flavors = dishDto.getFlavors();
        // 给每一个口味信息添加一个dish_id
        //给每一个菜品填充id值
        for(DishFlavor dishFlavor:flavors){
            dishFlavor.setDishId(dishDto.getId());
        };
        dishFlavorService.saveBatch(flavors); // 将口味传进去保存

    }
}
