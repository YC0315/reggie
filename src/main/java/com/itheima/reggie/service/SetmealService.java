package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.domain.Setmeal;
import com.itheima.reggie.dto.SetmealDto;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
     //新增套餐，同时需要保存套餐和菜品的关联关系

    public void saveWithDish(SetmealDto setmealDto);


     //删除套餐，同时需要删除套餐和菜品的关联数据
    public void removeWithDish(List<Long> ids);

    // 批量套餐停售
    public void stopsoles(List<Long> ids);

    // 批量套餐起售
    public void startsoles(List<Long> ids);

    public Page pageselect(int page, int pageSize, String name);

    // 修改套餐
    public SetmealDto modifySetmeal(Long id);

    public Result<String> SetmealWithDish(SetmealDto setmealDto);
}
