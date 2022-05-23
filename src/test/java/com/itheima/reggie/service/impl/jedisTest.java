package com.itheima.reggie.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import redis.clients.jedis.Jedis;

import java.util.Set;

// 使用jedis操作redis中的数据.Jedis是Redis的Java实现的客户端，其API提供了比较全面的Redis命令的支持
@SpringBootTest
public class jedisTest {

    // 注入redis的类,使用springdataredis操作redis
    @Autowired
    private RedisTemplate redisTemplate;


    // 使用jedis操作redis
    @Test
    public void testJedis() {
        // 1.获取连接
        Jedis jedis = new Jedis("localhost", 6379);

        //2.执行具体操作
        jedis.set("username", "xiaoming");

        String username = jedis.get("username");
        System.out.println(username);


        // 3.关闭连接
        jedis.close();
    }



    // 操作String类型的数据
    @Test
    public void testString() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("age", "20");
        Object age = valueOperations.get("age");
        System.out.println(age);
    }

    //通用操作
    @Test
    public void common(){
        Set<String> keys = redisTemplate.keys("*");
        for(String key:keys){
            System.out.println(key);
        }
    }

}
