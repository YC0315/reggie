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
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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

    // 要操做Redis，需要注入RedisTemplate对象
    @Autowired
    private RedisTemplate redisTemplate;

    // 改造操作redis的方式，使用注解去操作缓存
    private CacheManager cacheManager;

    // 数据源
    @Autowired
    private DataSource dataSource;

    // 新增菜品
    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto){  // 用一个封装类去接受参数，原本的catetory类不能接受了，因为属性和字段对不上，提交过来的是json,要加@RequestBody
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto); // 调用业务逻辑层接口

        // 改造：清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("dish_*");  // 获得所有以dish_开头的key，表示所有菜品类别的缓存
        redisTemplate.delete(keys);

        // 或者精确的清理，更新哪个类别的菜品就清理哪个菜品的缓存
        //动态的生成key
        //String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        //redisTemplate.delete(key);

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
        System.out.println(dishDtoPage.toString());

        return Result.success(dishDtoPage); // 返回一个分页构造器对象
    }

    // 根据id查询对应的菜品信息和口味信息
    @GetMapping("/{id}")
    //@Cacheable(value = "dishCache", key = "#id")  // 在方法执行前spring先查看缓存中是否有数据如果有则直接返回缓存数据，如果没有，调用方法并将方法的返回值放入缓存中
    public Result<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return Result.success(dishDto);
    }

    // 更新菜品
    @PutMapping
    // @CacheEvict(value = "dishCache", key = "#dishDto.id")  // 将菜品信息从缓存中删除
    public Result<String> update(@RequestBody DishDto dishDto){  // 用一个封装类去接受参数，原本的catetory类不能接受了，因为属性和字段对不上，提交过来的是json,要加@RequestBody
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto); // 调用业务逻辑层接口

        // 改造：清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("dish_*");  // 获得所有以dish_开头的key，表示所有菜品类别的缓存
        redisTemplate.delete(keys);

        // 或者精确的清理，更新哪个类别的菜品就清理哪个菜品的缓存
        //动态的生成key
        //String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        //redisTemplate.delete(key);
        return Result.success("更新菜品成功！");
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

        ///改造
        // 根据分类id先从缓存中取数据,缓存的是什么，是一个List<DishDto>
        List<DishDto> dishDtoList = null;
        //动态的构造一个"Key"
        String key = "dish_" + dish.getCategoryId() + "_" +dish.getStatus();
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key); // 获取到的是一个List对象，是每个分类的菜品

        // 如果存在数据则直接返回，无需查询数据库
        if(dishDtoList != null){
            return Result.success(dishDtoList);
        }

        // 如果缓存中不存在数据则执行查询，将查询到的菜品数据缓存到Redis中

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map((item) -> {
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

        //将查询到的菜品数据缓存到Redis中,存60分钟
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

        return Result.success(dishDtoList);
    }


    // 批量菜品停售
    @PostMapping("/status/0")
    public Result<String> stopsole(@RequestParam List<Long> ids) {
        // 清理所有菜品的缓存数据,涉及写操作的都要清除缓存
        Set keys = redisTemplate.keys("dish_*");  // 获得所有以dish_开头的key，表示所有菜品类别的缓存
        redisTemplate.delete(keys);
        dishService.updateStatus(ids);
        return Result.success("停售成功！");
    }

    // 批量菜品起售
    @PostMapping("/status/1")
    public Result<String> startsole(@RequestParam List<Long> ids) {
        // 清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("dish_*");  // 获得所有以dish_开头的key，表示所有菜品类别的缓存
        redisTemplate.delete(keys);
        dishService.updatestartStatus(ids);
        return Result.success("停售成功！");
    }

    // 批量菜品删除
    @DeleteMapping
    public Result<String> delsole(@RequestParam List<Long> ids){
        // 清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("dish_*");  // 获得所有以dish_开头的key，表示所有菜品类别的缓存
        redisTemplate.delete(keys);

        dishService.delssoles(ids);
        return Result.success("删除成功！");
    }


}

