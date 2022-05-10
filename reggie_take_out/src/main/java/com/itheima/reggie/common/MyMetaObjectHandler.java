package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// 公共字段填充类
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    // 插入时自动填充,在执行mybatisPlus的insert()时，自动给某些字段填充值，这样的话，我们就不需要手动给insert()里的实体类赋值了
    @Override
    public void insertFill(MetaObject metaObject) {
        // 填充创建时间，更新时间，创建人，更新人
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());  // 使用自己定义的BaseContext.getCurrentId()来获取当前登录用户的id,来动态的自动填充字段！！！！！！！
    }

    // 更新时自动填充
    @Override
    public void updateFill(MetaObject metaObject) {
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
