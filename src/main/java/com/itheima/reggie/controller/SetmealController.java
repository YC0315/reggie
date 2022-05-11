package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.domain.Category;
import com.itheima.reggie.domain.Dish;
import com.itheima.reggie.domain.Setmeal;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// 套餐管理
@RequestMapping("/setmeal")
@Slf4j
@RestController
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    //springcache的注解
    @Autowired
    private CacheManager cacheManager;


    // 新增套餐
    @PostMapping  // 不要忘了加@RequestBody

    // 当新增套餐时，需要将缓存数据一块清理掉,否则会使缓存和数据库中的数据不一致。value = "setmealCache"就是指我要删除的是setmealCache这个分类下面的缓存
    // allEntries = true表示要删除setmealCache这个分类下面的所有缓存数据
    @CacheEvict(value = "setmealCache", allEntries = true)
    public Result<String> save(@RequestBody SetmealDto setmealDto){  // 因为请求的参数不只是包含setmeal的属性，因此参数类型不能使用setmeal实体，要用一个扩展类，它继承setmeal并且有额外的属性
        setmealService.saveWithDish(setmealDto);
        return Result.success("添加套餐成功！");
    }

    // 套餐分页查询
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name){
        // 构建分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);

        Page<SetmealDto> dtoPage = new Page<>();

        // 创建条件构造器
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        // 设定筛选条件,根据name进行like查询
        lqw.like(name != null, Setmeal::getName, name);
        // 按更新时间排序
        lqw.orderByDesc(Setmeal::getUpdateTime);
        // 执行分页查询
        setmealService.page(pageInfo, lqw);  // 两个参数，一个是分页构造器对象，一个是条件构造器对象

        // 为了显示套餐分类的名称而不是显示id值，进行对象拷贝,并且忽略records值，因为类型不一样,返回一个SetmealDto对象
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        // 重新设置records
        List<Setmeal> records = pageInfo.getRecords();
        // 遍历records获得菜品对应的分类id，再查表获得分类名称
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();  // new一个对象，要设定其分类名称
            // 先将其他的属性拷贝给这个对象
            BeanUtils.copyProperties(item, setmealDto);
            // 获得菜品对应的id
            Long cateGoryId = item.getCategoryId();
            // 调用cateGory逻辑层的方法获得一个分类对象!!根据id获得一个对象。
            Category category = categoryService.getById(cateGoryId);
            if(category != null){
                String categoryName = category.getName();
                setmealDto.setName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        // 将新的records设置给dtoPage对象
        dtoPage.setRecords(list);
        return Result.success(dtoPage);
    }

    // 删除套餐
    @DeleteMapping
    // 当删除套餐时，需要将缓存数据一块清理掉,value = "setmealCache"就是指我要删除的是setmealCache这个分类下面的缓存
    // allEntries = true表示要删除setmealCache这个分类下面的所有缓存数据
    @CacheEvict(value = "setmealCache", allEntries = true)
    public Result<String> delete(@RequestParam List<Long> ids){
        // 删除套餐同时也要删除套餐关系表中所关联的菜品，因此需要操作两张表
        setmealService.removeWithDish(ids);
        return Result.success("删除成功！");
    }


    // 套餐停售
    @PostMapping("/status/0")
    public Result<String> stopsole(Long ids){
        Setmeal setmealServiceById = setmealService.getById(ids);
        setmealServiceById.setStatus(0);
        setmealService.updateById(setmealServiceById);
        return Result.success("停售成功！");
    }

    // 套餐起售
    @PostMapping("/status/1")
    public Result<String> startsole(Long ids){
        Setmeal setmealServiceById = setmealService.getById(ids);
        setmealServiceById.setStatus(1);
        setmealService.updateById(setmealServiceById);
        return Result.success("起售成功！");
    }

    // 根据条件查询套餐数据
    @GetMapping("/list")
    // 使用springcache注解缓存套餐数据,value是指缓存的名称，某一类缓存; key是redis中键的名称,需要人为指定，和当前的查询条件有关系，是一个动态拼出来的key,根据查询条件定
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    public Result<List<Setmeal>> save(Setmeal setmeal){
        // 构建条件查询器
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        // 设定条件
        lqw.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        lqw.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        lqw.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(lqw);
        return Result.success(list);
    }


}
