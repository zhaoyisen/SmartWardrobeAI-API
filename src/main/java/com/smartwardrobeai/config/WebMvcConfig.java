package com.smartwardrobeai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LogInterceptor logInterceptor;
    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截所有 API 请求，添加日志id
        registry.addInterceptor(logInterceptor).addPathPatterns("/**");
        // 拦截所有 API 请求，注入userId
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
    }
}