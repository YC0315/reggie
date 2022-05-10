package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.domain.Employee;

// service接口继承一个mybatisplus提供的类，再也不用写那些乱七八糟的方法了，泛型中是Employee实体类型
public interface EmployeeService extends IService<Employee> {
}
