package com.smartwardrobeai.config;

import com.aliyun.credentials.Client;
import com.aliyun.teaopenapi.models.Config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aliyun.sms")
@Data
public class AliyunSmsConfig {
    private String accessKeyId;
    private String accessKeySecret;
    private String signName;
    private String templateCode;
    private String schemeName;
    private String countryCode = "86"; // 默认中国区号
    private String endpoint = "dypnsapi.aliyuncs.com"; // 号码认证服务端点

    /**
     * 初始化阿里云号码认证服务客户端
     * 使用凭据初始化账号Client
     * 工程代码建议使用更安全的无AK方式，凭据配置方式请参见：https://help.aliyun.com/document_detail/378657.html
     */
    @Bean(name = "aliyunSmsClient")
    public com.aliyun.dypnsapi20170525.Client aliyunSmsClient() throws Exception {
        Config config = new Config();
        
        // 如果提供了AccessKeyId和AccessKeySecret，直接在Config上设置静态凭据
        if (accessKeyId != null && !accessKeyId.isEmpty() && 
            accessKeySecret != null && !accessKeySecret.isEmpty()) {
            // 使用静态AccessKey方式，直接在Config上设置
            config.setAccessKeyId(accessKeyId)
                  .setAccessKeySecret(accessKeySecret);
        } else {
            // 如果没有提供AccessKey，使用默认凭据（通过Credentials Client从环境变量、配置文件等自动获取）
            Client credential = new Client();
            config.setCredential(credential);
        }
        
        // Endpoint 设置为号码认证服务端点，请参考 https://api.aliyun.com/product/Dypnsapi
        config.setEndpoint(endpoint);
        
        return new com.aliyun.dypnsapi20170525.Client(config);
    }
}

