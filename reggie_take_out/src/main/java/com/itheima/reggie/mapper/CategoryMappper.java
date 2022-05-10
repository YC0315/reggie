package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.domain.Category;
import org.apache.ibatis.annotations.Mapper;

// 继承BaseMapper接口，里面有操作数据库的接口方法，泛型是操作的实体类名！！这是基于mybatisplus开发的dao层！
@Mapper  // 要加上@Mapper注解，否则容器里没有数据访问层的对象
public interface CategoryMappper extends BaseMapper<Category> {


}
