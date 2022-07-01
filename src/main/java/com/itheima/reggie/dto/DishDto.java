package com.itheima.reggie.dto;

import com.itheima.reggie.domain.Dish;
import com.itheima.reggie.domain.DishFlavor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
// 数据传输对象类，用于封装从页面传过来的参数，有一些参数不能用实体类的方式去接受，就定义一个数据传输类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DishDto extends Dish {


    private List<DishFlavor> flavors = new ArrayList<>();

    // 用于显示菜品名称而不是菜品的id
    private String categoryName;

    private Integer copies;
}
