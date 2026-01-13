package com.smartwardrobeai.config;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tencent.sms")
@Data
public class TencentSmsConfig {
    private String secretId;
    private String secretKey;
    private String region = "ap-beijing"; // 默认北京区域
    private String appId;
    private String signName;
    private String templateId;

    /**
     * 初始化腾讯云 SMS 客户端
     */
    @Bean(name = "tencentSmsClient")
    public SmsClient tencentSmsClient() {
        // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId 和 SecretKey
        Credential cred = new Credential(secretId, secretKey);

        // 实例化一个http选项，可选的，没有特殊需求可以跳过
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("sms.tencentcloudapi.com");
        httpProfile.setConnTimeout(60);
        httpProfile.setReadTimeout(60);

        // 实例化一个client选项，可选的，没有特殊需求可以跳过
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        // 指定签名算法，默认为 HmacSHA256
        clientProfile.setSignMethod("HmacSHA256");

        // 实例化 SMS 客户端对象
        return new SmsClient(cred, region, clientProfile);
    }
}

