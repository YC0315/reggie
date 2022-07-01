package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.domain.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

// 分类管理：菜品分类和套餐分类
@Slf4j  // 添加日志方便调试
@RestController  // 添加Rest风格的Controller注解
@RequestMapping("/category")  // 设定请求路径的父路径
public class CategoryController {

    // 注入业务逻辑层
    @Autowired
    private CategoryService categoryService;

    @Resource
    private RedisTemplate redisTemplate;

    // 新增菜品
    // post请求由于前端传入的是一个json数据，因此接受时要使用@RequestBody进行转换，并封装到employee实体中
    // @RequestBody的作用其实是将json格式的数据转为java对象
    @PostMapping
    public Result<String> save(@RequestBody Category category){  // 此处的返回值类型要看前端接受什么类型的参数类型
        categoryService.save(category);
        //String keys = "category_*";  // 获得所有以dish_开头的key，表示所有菜品类别的缓存
        //redisTemplate.opsForValue().set(keys, category);

        return Result.success("新增菜品成功！");
    }

    // 菜品分类和套餐分类的分页查询功能
    // 创建进行分页查询的方法,从前端传过来的只有两个数据，一个是page,一个是pagesize,并且发送的是Get请求，
    // get请求传过来的不是json数据，而是普通参数拼接到了URL后面
    @GetMapping("/page")

    public Result<Page> page(int page, int pageSize){  // 注意返回值是Result返回值类型是Page
        // 创建一个分页构造器,直接new一个page对象，传入两个参数(当前页，每页记录数），然后调用方法实现分页查询
        Page<Category> catetorypage = new Page<>(page, pageSize);

        // 创建一个条件构造器，new一个查询条件对象，根据sort排序
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        // 添加排序条件， 实例名称::实例方法等同于Category.getSort(xx).
        lqw.orderByAsc(Category::getSort);

        //调用服务层的查询方法，传入Page对象和查询条件对象
        categoryService.page(catetorypage, lqw);
        return Result.success(catetorypage);
    }

    // 删除菜品分类或者套餐分类
    @DeleteMapping
    public Result<String> delete(Long id){  // 这里形参的名字，尽量和前端传参过来的参数名称一致！！！！
        categoryService.removeById(id);
        return Result.success("分类删除成功！");
    }

    // 修改分类信息
    @PutMapping  // 传过来的是一个json数据，传过来的参数会封装到catelogy实体类中，然后更新数据库中整个实体类对应的表中所有字段的信息
    public Result<String> update(@RequestBody Category category){
        categoryService.updateById(category);
        return Result.success("信息修改成功！");
    }

    // 根据条件查询分类数据,根据传过来的条件动态的查询数据，并返回给页面，新增菜品时显示菜品分类下拉框中的list数据，即菜品的type为1的所有菜品
    @GetMapping("/list")
    public Result<List<Category>> list(Category category){
        // 最终的sql语句是SELECT id,type,name,sort,create_time,update_time,create_user,update_user FROM category WHERE (type = ?) ORDER BY sort ASC,update_time DESC

        // 条件构造器
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        // 添加条件
        lqw.eq(category.getType() != null,Category::getType, category.getType());  // 查询Category表中所有的Type字段值等于category.getType()值的那些实体类，也就是一行数据
        //添加排序条件
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(lqw);  // list方法用于查询一个列表
        return Result.success(list);
    }

}
