package com.itheima.reggie.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// mybatisplus分页插件
@Configuration
public class MybatisPlusConfig {
    // 通过拦截器的方式将分页插件加进来
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        // 设置拦截器
        MybatisPlusInterceptor mybatisPlusInterceptor  = new MybatisPlusInterceptor();
        //将分页插件加入拦截器
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        // 这里还可以添加其他的拦截器

        return mybatisPlusInterceptor;
    }

}
