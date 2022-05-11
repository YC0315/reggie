package com.itheima.reggie.common;


// 自定义的业务异常
public class CustomException extends RuntimeException{
    public CustomException(String msg){
        super(msg);

    }
}
