package com.itheima.reggie.controller;

import com.itheima.reggie.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

// 做文件的上传和下载使用
@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws IOException {  // 此处的参数名必须为file

        // file是一个临时文件，要将其转存到指定位置否则本次请求完成后临时文件会删除
        // 获得原始文件名
        String name = file.getOriginalFilename();
        file.transferTo(new File(basePath + name));
        return Result.success(name);  // 这里需要将菜品名称返回回去，因为上传了图片以后，新增菜品时要用到菜品名称
    }

    // 文件下载
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws Exception {
        // 输入流，通过输入流读取文件内容
        FileInputStream fileInputStream = new FileInputStream(new File(basePath+name));

        // 输出流，将文件写回浏览器，在浏览器展示图片
        ServletOutputStream outputStream = response.getOutputStream();

        response.setContentType("image/jpeg");
        // 什么时候读完了呢,带参数read()方法返回的是读取的字节数,到达流数据的末端返回值为-1
        int len = 0;
        byte[] bytes = new byte[1024];
        while((len = fileInputStream.read(bytes)) != -1){  // 每次读一个字节数组，然后输出
            outputStream.write(bytes, 0, len);
            outputStream.flush();
        }
        outputStream.close();
        fileInputStream.close();  // 关闭资源
    }
}
