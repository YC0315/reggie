package com.itheima.reggie.common;

// 基于ThreadLocal封装的工具类，用于保存和获取当前登录用户的id，因为在整个http请求过程中线程不会变，因此可以从线程中保存值和获取值,作用域是一个线程之内
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();  // 因为id的类型时Long，因此泛型中的数据类型是Long

    //工具类中的方法基本上都是静态的，直接用类名调用，不用实例化对象，方便

    // 保存值
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    // 获取值
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
