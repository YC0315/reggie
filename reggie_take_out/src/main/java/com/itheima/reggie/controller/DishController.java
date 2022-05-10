package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.domain.Category;
import com.itheima.reggie.domain.Dish;
import com.itheima.reggie.domain.DishFlavor;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// 菜品管理
// Dish和DishFlavor的controller用一个就可以
@RestController
@Slf4j
@RequestMapping("/dish")  // 指定父路径
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    // 新增菜品
    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto){  // 用一个封装类去接受参数，原本的catetory类不能接受了，因为属性和字段对不上，提交过来的是json,要加@RequestBody
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto); // 调用业务逻辑层接口
        return Result.success("新增菜品成功！");
    }

    // 菜品信息分页查询
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name){
        // 构建分页构造器对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        // 为了显示菜品的名称，创建一个DishDto类型的分页构造器对象
        Page<DishDto> dishDtoPage = new Page<>();

        // 创建条件构造器
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        // 设定筛选条件
        lqw.like(name != null, Dish::getName, name);
        // 按更新时间排序
        lqw.orderByDesc(Dish::getUpdateTime);
        // 执行分页查询
        dishService.page(pageInfo, lqw);  // 两个参数，一个是分页构造器对象，一个是条件构造器对象

        // 属性拷贝，将pageInfo的各个属性值拷贝到dishDtoPage对象上
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,  dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return Result.success(dishDtoPage); // 返回一个分页构造器对象
    }

    // 根据id查询对应的菜品信息和口味信息
    @GetMapping("/{id}")
    public Result<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return Result.success(dishDto);
    }

    // 更新菜品
    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto){  // 用一个封装类去接受参数，原本的catetory类不能接受了，因为属性和字段对不上，提交过来的是json,要加@RequestBody
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto); // 调用业务逻辑层接口
        return Result.success("新增菜品成功！");
    }

    // 套餐管理，根据条件查询菜品数据
    /*@GetMapping("/list")
    public Result<List<Dish>> list(Dish dish){
        //SELECT id,name,category_id,price,code,image,description,status,sort,create_time,update_time,create_user,update_user FROM dish WHERE (category_id = ?) ORDER BY sort ASC,update_time DESC

        // 构建条件构造器
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        // 设定条件
        lqw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //查询在售大的菜品
        lqw.eq(Dish::getStatus, 1);
        //添加排序条件,先根据sort排序，如果相等则根据更新时间排序
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(lqw);

        return Result.success(list);

    }*/

    @GetMapping("/list")
    public Result<List<DishDto>> list(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        return Result.success(dishDtoList);
    }


}

