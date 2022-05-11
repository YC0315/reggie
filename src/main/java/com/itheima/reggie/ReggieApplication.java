package com.itheima.reggie;

// 启动类，设置启动类以后就可以将springboot项目启动起来了

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Slf4j  // 提供日志，方便测试

//启动程序加上扫描注解 @ServletComponentScan 让过滤器生效
// springboot项目，所以导入注解
@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement // 开启事务支持
public class ReggieApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class, args);

        // 可以使用日志输出代替pringln打印输出
        log.info("启动了！");

    }
}
