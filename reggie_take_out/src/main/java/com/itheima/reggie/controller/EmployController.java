package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.domain.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j  // 添加日志方便调试
@RestController  // 添加Rest风格的Controller注解
@RequestMapping("/employee")  // 设定请求路径的父路径
public class EmployController {

    // 注入业务逻辑层
    @Autowired
    private EmployeeService employeeService;

    // 编写登录方法
    @PostMapping("/login")  // 前端发送的是一个post请求，所以使用postmapping,子路径就是/login,这样当有登录请求时就会调用这个login()方法
    public Result<Object> login(HttpServletRequest Request, @RequestBody Employee employee) {
        // post请求由于前端传入的是一个json数据，因此接受时要使用@RequestBody进行转换，并封装到employee实体中
        // @RequestBody的作用其实是将json格式的数据转为java对象。

        // 登录逻辑
        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库
        // 以下为Lambda格式的按条件查询，Employee::getUsername为查询名称字段，Employee里面的Username属性， eq表示是否相等
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>(); // 使用了mybatis plus的条件构造器LambdaQueryWrapper对象
        queryWrapper.eq(Employee::getUsername, employee.getUsername());  //调用LambdaQueryWrapper的eq方法，查询Employee::getUsername==employee.getUsername()的情况
        Employee emp = employeeService.getOne(queryWrapper);

        //3、如果没有查询到则返回登录失败结果
        if(emp == null){
            return Result.error("用户名错误，登录失败！");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return Result.error("密码不正确，登录失败！");
        }

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus() == 0){
            return Result.error("用户已禁用，请用其他账号登录！");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        Request.getSession().setAttribute("employee", emp.getId());

        // 查询成功，返回查询到的对象
        return Result.success(emp);
    }

    // 退出登录
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest Request){
        // 清除session中保存当前用户的id
        Request.getSession().removeAttribute("employee");
        return Result.success("退出成功！");
    }

    /*
    * 员工登录功能之
    * 新增员工
    */

    @PostMapping
    public Result<String> save(HttpServletRequest request, @RequestBody Employee employee){  // 传过来的是json数据，不要忘了添加@RequestBody，
                                                                 // 该注解用于读取Request请求的body部分数据，使用系统默认配置的HttpMessageConverter进行解析，然后把相应的数据绑定到要返回的对象上，再把HttpMessageConverter返回的对象数据绑定到 controller各种方法的参数上。
        log.info("新增员工信息:{}", employee.toString());

        // 除了在前端页面上提交的那几条个人信息已经存入employee里了以外还要设置一些其他值
        // 给每个新增员工设置一个初始密码123456，使用 阿贾克斯加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        // 以下可以使用公共字段自动填充
        /*// 新增用户时间
        employee.setCreateTime(LocalDateTime.now());
        // 新增用户的更新时间
        employee.setUpdateTime(LocalDateTime.now());
        // 获得当前用户的id
        Long empId = (Long) request.getSession().getAttribute("employee");
        // 新增用户的创建人以及更新人
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);*/

        // 调用service层接口中的保存方法，这个方法并不是我们在service层的实现类中自己写的，而是继承了IService类，在这个类中官方提供的
        employeeService.save(employee);

        return Result.success("新增用户成功！");   // controller层最终都要返回一个Result给前端
    }

    // 创建进行分页查询的方法,从前端传过来的只有两个数据，一个是page,一个是pagesize,并且发送的是Get请求，
    // get请求传过来的不是json数据，而是普通参数拼接到了URL后面
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name){
    //  log.info("page={}, pagesize={}, name={}", page,pageSize,name);

        // 构造一个分页构造器，参数是：一共几页，每页几条数据
        Page mypage = new Page(page, pageSize);

        // 构造一个条件构造器
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper();
        // 设置过滤条件，否则当搜索一个人时不会显示结果
        if(name != null){
            lqw.like(Employee::getName, name);
        }
        // 设置排序条件
        lqw.orderByDesc(Employee::getUpdateTime);

        // 执行查询
        //SELECT id,username,name,password,phone,sex,id_number,status,create_time,update_time,create_user,update_user FROM employee ORDER BY update_time DESC LIMIT ?
        //SELECT id,username,name,password,phone,sex,id_number,status,create_time,update_time,create_user,update_user FROM employee WHERE (name LIKE ?) ORDER BY update_time DESC LIMIT ?
        employeeService.page(mypage, lqw);  // 将分页构造器和条件构造器传进去当成参数，page方法里面有select

        return Result.success(mypage);
    }

    // 更新操作，根据员工id修改信息,由于传过来的是json数据，因此要使用@RequestBody进行转换。启用禁用是更新用户信息，编辑员工信息也是更新用户信息
    @PutMapping  // 没有子路径，只请求到/employee，因此不需要写子路径
    public Result<String> update(HttpServletRequest request , @RequestBody Employee employee){

        // 设置更新时间以及更新人
        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);

        // UPDATE employee SET status=?, update_time=?, update_user=? WHERE id=?
        employeeService.updateById(employee); // employees实体中已经有了id和status两个参数值，更新数据库中的表时会只更新这两个字段值

        return Result.success("用户信息更新成功!");
    }

    // 编辑员工信息时，要先将员工的信息显示在复选框中再去修改，修改完提交时，还是调用的上面的update方法，UPDATE employee SET username=?, name=?, password=?, phone=?, sex=?, id_number=?, status=?, create_time=?, update_time=?, create_user=?, update_user=? WHERE id=?
    @GetMapping("/{id}")  // 不要将""里面的内容写错了，/在{}的外面，里面是参数
    public Result<Employee> getById(@PathVariable Long id){ // @PathVariable能使传过来的参数绑定到方法的参数上

        log.info("根据id查询员工信息。。");
        //SELECT id,username,name,password,phone,sex,id_number,status,create_time,update_time,create_user,update_user FROM employee WHERE id=?
        Employee employee = employeeService.getById(id);

        if(employee != null){
            return Result.success(employee);
        }
        return Result.error("没有查询到该员工信息！");
    }





}
