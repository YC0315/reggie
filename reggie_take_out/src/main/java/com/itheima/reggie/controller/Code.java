package com.itheima.reggie.controller;

// 提供code值
public class Code {

    // 1结尾代表成功  0结尾代表失败

    // 增删改查成功
    public static final Integer SAVE_OK = 20011;
    public static final Integer UPDATE_OK = 20021;
    public static final Integer DELETE_OK = 20031;
    public static final Integer GET_OK = 20041;

    // 增删改查失败
    public static final Integer SAVE_ERR = 20010;
    public static final Integer UPDATE_ERR = 20020;
    public static final Integer DELETE_ERR = 20030;
    public static final Integer GET_ERR = 20040;

}
