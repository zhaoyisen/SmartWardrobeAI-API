package com.smartwardrobeai.config;

import com.aliyun.teaopenapi.models.Config;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Aliyun配置
 */
@Data
@Configuration
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "aliyun")
public class AliyunConfig {
//    // 阿里云号码认证服务配置
//    @Value("${aliyun.access-key-id:}")
//    private String aliyunAccessKeyId;
//
//    @Value("${aliyun.access-key-secret:}")
//    private String aliyunAccessKeySecret;
//
//    /**
//     * 图片分割配置
//     */
//    @Value("${aliyun.imageseg.endpoint:}")
//    private String aliyunImagesegEndpoint;
//
//    @Value("${aliyun.imageseg.connect-timeout:}")
//    private String aliyunImagesegConnectTimeout;
//
//    @Value("${aliyun.imageseg.read-timeout:}")
//    private String aliyunImagesegReadTimeout;
//
//    /**
//     * 短信配置
//     */
//    @Value("${aliyun.sms.sign-name:}")
//    private String aliyunSmsSignName;
//
//    @Value("${aliyun.sms.template-code:}")
//    private String aliyunTemplateCode;
//
//    @Value("${aliyun.sms.scheme-name:}")
//    private String aliyunSchemeName;
//
//    @Value("${aliyun.sms.country-code:86}")
//    private String aliyunCountryCode;
//
//    @Value("${aliyun.sms.endpoint:dypnsapi.aliyuncs.com}")
//    private String aliyunSmsEndpoint;
// ================== 1. 属性映射区域 (对应 YAML) ==================

    /**
     * 全局 AccessKeyId
     */
    private String accessKeyId;

    /**
     * 全局 AccessKeySecret
     */
    private String accessKeySecret;

    /**
     * 图像分割配置 (嵌套对象)
     */
    private Imageseg imageseg = new Imageseg();

    /**
     * 短信配置 (嵌套对象)
     */
    private Sms sms = new Sms();

    /**
     * 内部静态类：对应 imageseg 层级
     */
    @Data
    public static class Imageseg {
        private String endpoint="imageseg.cn-shanghai.aliyuncs.com";
        private int connectTimeout = 5000; // 默认值
        private int readTimeout = 10000;   // 默认值
    }

    /**
     * 内部静态类：对应 sms 层级
     */
    @Data
    public static class Sms {
        private String signName;
        private String templateCode;
        private String schemeName;
        private String countryCode;
        private String endpoint;
    }


    // ================== 2. Bean 定义区域 (创建 Client) ==================

    /**
     * Bean 1: 图像分割客户端
     */
    @Bean
    public com.aliyun.imageseg20191230.Client imageSegClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(this.accessKeyId)
                .setAccessKeySecret(this.accessKeySecret);

        config.endpoint = this.imageseg.getEndpoint();
        config.readTimeout = this.imageseg.getReadTimeout();
        config.connectTimeout = this.imageseg.getConnectTimeout();

        return new com.aliyun.imageseg20191230.Client(config);
    }

    /**
     * Bean 2: 短信客户端 (Dysmsapi)
     * 注意：这里使用了 dysmsapi (短信服务)，如果你确认是用 dypns (号码认证)，请换回 dypns 的 Client 类
     */
    @Bean
    public com.aliyun.dypnsapi20170525.Client smsClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(this.accessKeyId)
                .setAccessKeySecret(this.accessKeySecret);

        // 短信服务的 Endpoint 通常是 dysmsapi.aliyuncs.com
        // 如果你的 YAML 配置的是 dypnsapi，请确认你的业务类型，此处优先使用 YAML 配置的值
        config.endpoint = (this.sms.getEndpoint() != null && !this.sms.getEndpoint().isEmpty())
                ? this.sms.getEndpoint()
                : "dysmsapi.aliyuncs.com";

        return new com.aliyun.dypnsapi20170525.Client(config);
    }

}
