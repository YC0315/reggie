package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.domain.Employee;
import com.itheima.reggie.mapper.EmployeeMapper;
import com.itheima.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service  // 生成bean对象放在容器里
// ServiceImpl<EmployeeMapper, Employee> ，EmployeeMapper是mapper层的接口，因为要自动注入，所以要有。Employee是Service层方法的返回值类型
// 这样就不用写成员变量自动注入以及service中的方法了！！
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {


}
