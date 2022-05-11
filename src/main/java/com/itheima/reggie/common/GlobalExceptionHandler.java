package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;

// 全局的异常处理器，整个项目出现异常都在这处理 RestControllerAdvice = ControllerAdvice + ResponseBody
@RestControllerAdvice(annotations = {RestController.class, Controller.class})  // 声明这个类是用作异常处理的,并且声明拦截哪些controller的异常
//@ResponseBody  // 方法返回json数据，最终会把我们的结果封装成json数据返回
@Slf4j
public class GlobalExceptionHandler {

    // 拦截一种异常，数据库中的username不能重复异常
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)  // 写一个异常的处理标志，ExceptionHandler的作用是用来捕获指定的异常,处理什么种类的异常
    public Result<String> exception(SQLIntegrityConstraintViolationException ex){

        // 检查是否是username已经重复异常
        if(ex.getMessage().contains("Duplicate entry")){
            String[] s = ex.getMessage().split(" ");
            String msg = s[2] + "已存在！";
            return Result.error(msg);
        }
        return Result.error("未知错误！");
    }

    // 拦截一种异常，菜品分类或套餐分类不能删除
    @ExceptionHandler(CustomException.class)  // 写一个异常的处理标志，ExceptionHandler的作用是用来捕获指定的异常,处理什么种类的异常
    public Result<String> exception(CustomException ex){

        return Result.error(ex.getMessage());
    }

}
