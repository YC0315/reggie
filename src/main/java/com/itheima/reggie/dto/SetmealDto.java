package com.itheima.reggie.dto;

import com.itheima.reggie.domain.Setmeal;
import com.itheima.reggie.domain.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    //提供订单的分类名称，而不是只查询到分类的id
    private String categoryName;
}
