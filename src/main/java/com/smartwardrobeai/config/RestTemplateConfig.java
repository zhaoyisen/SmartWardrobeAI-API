package com.smartwardrobeai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置类
 * 用于将 RestTemplate 注册为 Bean，并设置超时时间
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // 1. 创建工厂类
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 2. 设置连接超时 (毫秒) - 10秒
        // 指的是与阿里云服务器建立 TCP 连接的时间
        factory.setConnectTimeout(10000);

        // 3. 设置读取超时 (毫秒) - 120秒 (2分钟)
        // 🔥 重点：AI 模型(特别是思考模式)生成内容很慢，这里必须设大一点！
        // 如果设得太短(比如5秒)，AI 还在思考，你的程序就报错 "Read timed out" 了。
        factory.setReadTimeout(120000);

        // 4. 创建 RestTemplate 实例
        return new RestTemplate(factory);
    }
}