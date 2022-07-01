package com.itheima.reggie.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 增加swagger的配置，swagger的作用:通过后端代码产生能够让前台开发或测试人员能够看懂的文档
 */
@Configuration  // @Configuration相当于写一个applicationContext.xml
@EnableSwagger2  // 开启swagger配置
public class SwaggerConfig extends WebMvcConfig {
    /**
     * 配置swagger
     * swagger有自己的配置实例对象 Docket
     * 这个对象方法中有多个属性
     * apiinfo属性是配置swagger显示的信息
     * select()配置扫描接口
     * groupname()配置api文档的分组 配置多个Docket实例可以实现多个分组，多个@bean
     * <p>
     * 常用注解
     *
     * @Api 是类上注解 控制了整个类生成接口信息的内容，属性tags 类的名字 description描述
     * @ApiOperation 写在方法上的注解，对方法进行描述， 属性value 方法描述 notes 提示信息
     * @ApiParam 写在方法参数中的注解，用于对参数进行描述，说明一下是否是必填项，属性有 name 参数名字 value参数描述 required是否是必须
     * @ApiModel是类上注解，主要应用在实体类上，属性value 类名称，description 是描述
     * @ApiModelproperty可以应用在方法上或是属性上，用于方法参数是应用类型时进行定义描述
     * @Apilgnore 用于类上方法上参数上，表示被忽视，
     */
    @Bean
    // 配置Docket对象，相当于是配置swagger的显示信息
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("外卖项目")
                .apiInfo(this.apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build();
    }

    // 生成的内容里面 产生接口信息
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("外卖项目接口文档")
                .version("1.0")
                .description("服务端和移动端接口，2022.06.13")
                .build();
    }
}
