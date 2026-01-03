package com.smartwardrobeai.config;

import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "qiniu.kodo")
@Data
public class QiniuConfig {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String domain;

    /**
     * 配置上传管理器 (注意：这里是 com.qiniu.storage.Configuration)
     */
    @Bean
    public com.qiniu.storage.Configuration qiniuConfiguration() {
        // Region.autoRegion() 会自动判断你的 bucket 在哪个区（华东、华北等）
        return new Configuration(Region.autoRegion());
    }

    /**
     * 构建上传管理器
     */
    @Bean
    public UploadManager uploadManager(com.qiniu.storage.Configuration config) {
        return new UploadManager(config);
    }

    /**
     * 认证工具类
     */
    @Bean
    public Auth auth() {
        return Auth.create(accessKey, secretKey);
    }
}