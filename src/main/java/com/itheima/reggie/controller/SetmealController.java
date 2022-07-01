package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.domain.Dish;
import com.itheima.reggie.domain.Setmeal;
import com.itheima.reggie.domain.SetmealDish;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private DishService dishService;

    // 新增套餐
    @PostMapping  // 不要忘了加@RequestBody
    // 当新增套餐时，需要将缓存数据一块清理掉,否则会使缓存和数据库中的数据不一致。value = "setmealCache"就是指我要删除的是setmealCache这个分类下面的缓存
    // allEntries = true表示要删除setmealCache这个分类下面的所有缓存数据
    @CacheEvict(value = "setmealCache", allEntries = true)//@CacheEvict 就是一个触发器，在每次调用被它注解的方法时，就会触发删除它指定的缓存的动作
    public Result<String> save(@RequestBody SetmealDto setmealDto) {  // 因为请求的参数不只是包含setmeal的属性，因此参数类型不能使用setmeal实体，要用一个扩展类，它继承setmeal并且有额外的属性
        setmealService.saveWithDish(setmealDto);
        return Result.success("添加套餐成功！");
    }

    // 套餐分页查询
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        Page dtoPage = setmealService.pageselect(page, pageSize, name);
        return Result.success(dtoPage);
    }

    // 删除套餐
    @DeleteMapping
    // 当删除套餐时，需要将缓存数据一块清理掉,value = "setmealCache"就是指我要删除的是setmealCache这个分类下面的缓存
    // allEntries = true表示要删除setmealCache这个分类下面的所有缓存数据
    @CacheEvict(value = "setmealCache", allEntries = true)
    public Result<String> delete(@RequestParam List<Long> ids) {
        // 删除套餐同时也要删除套餐关系表中所关联的菜品，因此需要操作两张表
        setmealService.removeWithDish(ids);
        return Result.success("删除成功！");
    }

    //套餐批量停售
    @PostMapping("/status/0")
    @CacheEvict(value = "setmealCache", allEntries = true)
    public Result<String> stopsole(@RequestParam List<Long> ids) {  // 多个请求参数时要加@RequestParam注解
        setmealService.stopsoles(ids);
        return Result.success("套餐批量停售成功！");
    }

    //套餐批量起售
    @PostMapping("/status/1")
    @CacheEvict(value = "setmealCache", allEntries = true)
    public Result<String> startsole(@RequestParam List<Long> ids) {  // 多个请求参数时要加@RequestParam注解
        setmealService.startsoles(ids);
        return Result.success("套餐批量起售成功！");
    }



    // 根据条件查询套餐数据
    @GetMapping("/list")
    // 使用springcache注解缓存套餐数据,value是指缓存的名称，某一类缓存; key是redis中键的名称,需要人为指定，和当前的查询条件有关系，是一个动态拼出来的key,根据查询条件定
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    public Result<List<Setmeal>> save(Setmeal setmeal) {
        // 构建条件查询器
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        // 设定条件
        lqw.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        lqw.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        lqw.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(lqw);
        return Result.success(list);
    }

    // 移动端点击套餐显示具体菜品，需查询套餐菜品关系表和菜品表
    @GetMapping("/dish/{id}")
    public Result<List<Dish>> dish(@PathVariable("id") Long setmealid){
        // 设置条件构造器
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId, setmealid);
        List<SetmealDish> setmealDishes = setmealDishService.list(lqw);  //查出对应有哪些套餐

        //查出对应套餐后，查询套餐里面的菜品
        LambdaQueryWrapper<Dish> lqw2 = new LambdaQueryWrapper<>();
        ArrayList<Long> dishes = new ArrayList<>();
        for(SetmealDish setmealDish:setmealDishes){
            Long dishId = setmealDish.getDishId();
            dishes.add(dishId);
        }
        lqw2.in(Dish::getId,dishes);  //查找dishId在dishes里面的菜品，放到集合中
        List<Dish> dishList = dishService.list(lqw2);
        return Result.success(dishList);
    }

    // 修改套餐-1.回显
    @GetMapping("/{id}")
    public Result<SetmealDto> modify(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.modifySetmeal(id);
        return Result.success(setmealDto);
    }

    // 修改套餐-2.修改保存
    @PutMapping
    @CacheEvict(value = "setmealCache", allEntries = true)  // 删除缓存
    public Result<String> updata(@RequestBody SetmealDto setmealDto){
        Result<String> stringResult = setmealService.SetmealWithDish(setmealDto);
        return stringResult;
    }
}
