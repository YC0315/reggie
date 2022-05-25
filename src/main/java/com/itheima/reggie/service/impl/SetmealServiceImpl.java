package com.itheima.reggie.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.domain.Category;
import com.itheima.reggie.domain.Setmeal;
import com.itheima.reggie.domain.SetmealDish;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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

    @Autowired
    private CategoryService categoryService;

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

    // 套餐批量停售
    @Override
    public void stopsoles(List<Long> ids) {
        for (Long id : ids) {
            Setmeal setmealServiceById = this.getById(id);
            setmealServiceById.setStatus(0);
            this.updateById(setmealServiceById);
        }
    }
    // 套餐批量起售
    @Override
    public void startsoles(List<Long> ids) {
        for (Long id : ids) {
            Setmeal setmealServiceById = this.getById(id);
            setmealServiceById.setStatus(1);
            this.updateById(setmealServiceById);
        }
    }

    @Override
    public Page pageselect(int page, int pageSize, String name) {
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
        this.page(pageInfo, lqw);  // 两个参数，一个是分页构造器对象，一个是条件构造器对象

        // 为了显示套餐分类的名称而不是显示id值，进行对象拷贝,并且忽略records值，因为类型不一样,返回一个SetmealDto对象
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        // 重新设置records
        List<Setmeal> records = pageInfo.getRecords();
        // 遍历records获得菜品对应的分类id，再查表获得分类名称
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();  // new一个对象，要设定其分类名称
            // 先将其他的属性拷贝给这个对象
            BeanUtils.copyProperties(item, setmealDto);
            // 获得套餐对应的分类id
            Long cateGoryId = item.getCategoryId();
            // 调用cateGory逻辑层的方法获得一个分类对象!!根据id获得一个对象。
            Category category = categoryService.getById(cateGoryId);
            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        // 将新的records设置给dtoPage对象
        dtoPage.setRecords(list);
        return dtoPage;
    }

    //修改套餐-1.回显套餐
    @Override
    public SetmealDto modifySetmeal(Long id) {
        // setmeal表中没有套餐所包含的菜品这个字段，因此要封装到Setmeal实体类中
        //1.查询套餐信息封装到setmeal实体类中
        Setmeal setmeal = this.getById(id);
        //2.创建setmealDto实体类
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);

        //3.根据套餐id查询套餐菜关系表
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> dishList = setmealDishService.list(lqw);
        setmealDto.setSetmealDishes(dishList);
        return setmealDto;
    }

    //修改套餐-2.回显后，修改完保存
    @Override
    @Transactional
    public Result<String> SetmealWithDish(SetmealDto setmealDto) {
        if(setmealDto == null){
            return Result.success("请求异常！");
        }
        if(setmealDto.getSetmealDishes() == null){
            return Result.success("请添加菜品！");
        }
        // 更新套餐表
        this.updateById(setmealDto);
        // 查询套餐对应的菜品
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,setmealDto.getId());  // 查出表中当前套餐的id
        setmealDishService.remove(lqw);  //清楚套餐原来对应的菜品数据

        // 添加新的菜品
        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        //对于菜品表中新增的每一个菜品，设置其对应的套餐id
        for(SetmealDish setmealDish:dishes){
            setmealDish.setSetmealId(setmealDto.getId());
        }
        setmealDishService.saveBatch(dishes); // 将新添加的菜品保存到套餐菜品关系表中去
        return Result.success("套餐修改成功!");
    }





}
