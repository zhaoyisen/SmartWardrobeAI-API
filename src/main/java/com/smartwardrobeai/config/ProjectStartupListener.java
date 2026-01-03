package com.smartwardrobeai.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 项目启动监听器
 * 作用：在项目启动完成后，自动打印 Swagger 接口文档地址
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ProjectStartupListener implements ApplicationListener<WebServerInitializedEvent> {

    private final Environment env;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        try {
            // 1. 获取本机 IP 地址
            String ip = InetAddress.getLocalHost().getHostAddress();

            // 2. 获取实际运行的端口 (event.getWebServer().getPort() 能拿到真实端口)
            int port = event.getWebServer().getPort();

            // 3. 获取 Context Path (如果没有配置，默认为空字符串)
            String contextPath = env.getProperty("server.servlet.context-path", "");
            String path = (contextPath == null || contextPath.isEmpty() || "/".equals(contextPath))
                    ? ""
                    : (contextPath.startsWith("/") ? contextPath : "/" + contextPath);
            // 4. 获取应用名称
            String appName = env.getProperty("spring.application.name", "SmartWardrobe-AI");

            // 5. 拼接 Swagger UI 地址
            // SpringDoc 默认地址: /swagger-ui/index.html
            String localUrl = String.format("http://localhost:%d%s/swagger-ui/index.html", port, path);
            String externalUrl = String.format("http://%s:%d%s/swagger-ui/index.html", ip, port, path);

            // 6. 打印炫酷的日志
            log.info("""
                    
                    ----------------------------------------------------------
                    \t应用 '{}' 启动成功! 
                    \t接口文档(Local): \t{}
                    \t接口文档(Network): \t{}
                    ----------------------------------------------------------
                    """, appName, localUrl, externalUrl);

        } catch (UnknownHostException e) {
            log.warn("无法获取本机 IP 地址，仅打印 Localhost 地址");
        }
    }
}