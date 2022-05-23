package com.itheima.reggie.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;

/**
 * 短信发送工具类
 */
public class SMSUtils {


    public static void sendMessage(){
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI5tAyUVnUyxHvVto3GKjv", "G78fKCV3QdOoJAyxaiDu3csRnWAMtj");
        IAcsClient client = new DefaultAcsClient(profile);

        SendSmsRequest request = new SendSmsRequest();
        request.setSysRegionId("cn-hangzhou");
        request.setSignName("阿里云短信测试");
        request.setTemplateCode("SMS_154950909");
        request.setPhoneNumbers("17865313980");
        request.setTemplateParam("{\"code\":\"1234\"}");
        try {
            SendSmsResponse response = client.getAcsResponse(request);
            System.out.println("短信发送成功");
        }catch (ClientException e) {
            e.printStackTrace();
        }
    }
}


