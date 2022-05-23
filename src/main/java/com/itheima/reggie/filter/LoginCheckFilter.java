package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// 完善登录功能，使得用户必须登录后才能访问内部数据，而不是直接输入网址就可以访问后端，使用拦截器或者过滤器实现请求的拦截
// 指定拦过滤器的名称以及拦截哪些请求，这里会过滤全部请求，@WebFilter用于将一个类声明为过滤器，该注解将会在部署时被容器处理时被容器处理，启动程序加上扫描注解 @ServletComponentScan 让过滤器生效
@WebFilter(filterName= "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {  // 实现filter接口

    // 路径匹配器，用于过滤url是否放行，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest Request = (HttpServletRequest) servletRequest;  // 父类转换成子类，需要向下转型，强制类型转换
        HttpServletResponse Response = (HttpServletResponse) servletResponse;


        // 完善登录功能
        //1、获取本次请求的URI
        String url = Request.getRequestURI();

        // 放过一些不需要处理的请求
        String[] urls = new String[]{
                "/employee/login",
                "/employee.logout",
                "/backend/**",
                //"/front/**",
                "/common/**",
                "user/sendMsg",
                "user/login",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };

        //2、判断本次请求是否需要处理，本次请求的url是否匹配上了以上写的不应该处理的url请求
        boolean result = check(urls, url);

        //3、如果不需要处理，则直接放行
        if(result){
            filterChain.doFilter(Request, Response);// 放行
            return;
        }

        //4-1、判断登录状态，如果已登录，则直接放行
        if(Request.getSession().getAttribute("employee")  != null){  // 判断用户是否已经登录，用户 已经登录的话会有一个唯一的标识

            // 保存当前线程中登录用户的id,用来动态的自动填充字段
            Long employeeid = (Long) Request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(employeeid);

            filterChain.doFilter(Request, Response);// 放行，有操作的话，一定要在放行之前
            return;
        }

        //4-2、判断登录状态，如果已登录，则直接放行
        if(Request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}",Request.getSession().getAttribute("user"));

            Long userId = (Long)Request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(Request,Response);
            return;
        }

        //5、如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据,前端会自动跳转，因此只需要满足前端的要求即可
        Response.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));  // 前端要的是一个json数据，要将打印的消息封装到Result里然后转换成JSON
        return;
    }

    // 路径匹配，检查此次url是否需要放行
    public boolean check(String[] urls, String requestUrl){
            for(String url:urls){
                boolean match = PATH_MATCHER.match(url, requestUrl);
                if(match){
                    return true;
                }
            }
            return false;
    }
}
