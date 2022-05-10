package com.itheima.reggie.controller;

// 前后端沟通的协议，将增删改查的结果以及是否成功，成功失败后该返回什么封装到data模型中
public class Result1<E> {
    // 提供三个属性
    private Object data;  // 用来封装数据
    private Integer code; // 状态码code，表示成功还是失败
    private String msg;   // 消息，失败后展示什么

    // 提供构造方法
    public Result1() {
    }

    // 不带消息的构造方法
    public Result1(Integer code, Object data) {
        this.data = data;
        this.code = code;
    }

    // 带消息的构造方法
    public Result1(Integer code, Object data, String msg) {
        this.data = data;
        this.code = code;
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
