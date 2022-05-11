package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.domain.Category;

// service接口继承一个mybatisplus提供的类，再也不用写那些乱七八糟的方法了，泛型中是Category实体类型
public interface CategoryService extends IService<Category> {
    public void remove(Long ids);
}
