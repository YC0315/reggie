package com.itheima.reggie.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.ReadFrom;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

// 使用自己的序列化器StringRedisSerializer，要不然输出的都是乱码，默认的key-value都是Object类型的
@Configuration
@Slf4j
public class RedisConfig extends CachingConfigurerSupport {
    // 配置redis读写分离，可以在配置类也可以在启动类
    @Bean
    public LettuceClientConfigurationBuilderCustomizer ccbc(){
        return new LettuceClientConfigurationBuilderCustomizer() {
            @Override
            public void customize(LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigurationBuilder) {
                // 这里配置的是从读从Redis,写主Redis
                clientConfigurationBuilder.readFrom(ReadFrom.REPLICA_PREFERRED);
            }
        };
    }
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // 创建redisTemlate对象，要求是Key为String字符串，Value随意，可以定义成Object类型
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        // 设置连接工厂
        redisTemplate.setConnectionFactory(connectionFactory);

        // 默认的Key序列化器是：JdkSerializationRedisSerializer，会出现乱码
        // Key采用String序列化
        redisTemplate.setKeySerializer(new StringRedisSerializer()); // key序列化
        // Value采用JSON序列化，可以用于接收一个对象，会自动序列化和反序列化
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // value序列化

        return redisTemplate;
    }
}
