package com.itheima.reggie.config;

import com.itheima.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j  // 使用日志
// 配置静态资源映射,静态资源就是指图片，视频音频，html文件等,当收到网址请求时，拆解url映射到有资源的文件夹，实现资源的访问
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport  {

    // 重写方法，设置静态资源映射，比如浏览器请求的是http://localhost:8080/backend/index.html，此时就会映射到backend下的index.html文件
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {

        log.info("开始静态资源映射...");

        // 设置要映射到哪个访问路径，要访问哪些资源
        // 只要是/backend/**这种网络请求，都会映射到/backend/文件夹下,就回去/backend/下面找资源就可以访问到了。
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }

    // 扩展springmvc提供的消息框架的转换器,设置自己的消息类型转换器，重写设置消息类型转换方法,将Long型数据转换成String类型，避免Long型数据精度丢失问题（最长16位）
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 创建消息转换器对象
        MappingJackson2HttpMessageConverter mh = new MappingJackson2HttpMessageConverter();
        // 设置对象转换器，底层使用Jackson将java对象转换成json
        mh.setObjectMapper(new JacksonObjectMapper());
        // 将自己的消息转换器添加到mvc消息转换器集合中，放在集合第一位
        converters.add(0, mh);

    }
}
