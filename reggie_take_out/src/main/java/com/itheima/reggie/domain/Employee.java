package com.itheima.reggie.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

// 员工实体类 @Data注解表示在创建实体类时再也不用自己手写set和get和toString方法了，只需要提供私有属性即可
@Data
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;  // mybatisplus中整型都是Long类型的必须要加L

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    private String idNumber;

    private Integer status;


    //我们可以实现在进行插入（insert）操作时对添加了注解@TableField(fill = FieldFill.INSERT)的字段进行自动填充
    //后面会写配置自动填充的配置类，该配置类的作用用于配置自动填充的值
    //这样我们在具体业务中对实体类进行赋值就可以不用对这些公共字段进行赋值，在执行插入或者更新时就能自动赋值并插入数据库。
    //那么要自动赋的值在哪里配置？在项目的config包下新建自动填充处理类使其实现接口MetaObjectHandler，并重写其方法：

    // 公共字段（createUser， createTame...）自动填充，比如说，更新人 更新时间，创建人，创建时间
    // 指定处理策略fill = FieldFill.
    @TableField(fill = FieldFill.INSERT)  // 插入时填充字段
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)  // 插入和更新时填充字段
    private Long updateUser;

    @TableField(fill = FieldFill.INSERT)  // 插入时填充字段
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)  // 插入和更新时填充字段
    private LocalDateTime updateTime;



}
