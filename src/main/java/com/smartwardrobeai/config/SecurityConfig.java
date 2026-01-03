package com.smartwardrobeai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwardrobeai.common.Result;
import com.smartwardrobeai.common.ResultCode;
import com.smartwardrobeai.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // 使用 Lombok 自动注入 jwtAuthenticationFilter
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper; // Spring 自带的 JSON 转换器

    /**
     * 1. 注册密码加密器 (PasswordEncoder)
     * 作用：
     * - 告诉 Spring 容器，项目中涉及到密码加密时，使用 BCrypt 算法。
     * - 解决 AuthService 中注入 PasswordEncoder 报错的问题。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 2. 配置安全过滤器链 (2)
     * 作用：
     * - 定义哪些 URL 是公开的（如登录、注册）。
     * - 定义哪些 URL 是需要保护的（如上传衣物）。
     * - 关闭 CSRF 和 Session（因为我们用 JWT，不需要这些）。
     * - 将我们的 JwtAuthenticationFilter 插入到执行链路中。
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 2.1 禁用 CSRF (跨站请求伪造保护)，因为我们是无状态的 REST API，不需要它
                .csrf(AbstractHttpConfigurer::disable)

                // 2.2 设置 Session 管理策略为 "无状态" (Stateless)
                // 这意味着服务器不会在内存中保存用户的登录 session，每次请求都得带 Token
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 2.3 配置请求权限
                .authorizeHttpRequests(auth -> auth
                        // 允许匿名访问的接口 (注册、登录)
                        .requestMatchers("/api/auth/**",
                                "/test/**")
                        .permitAll()
                        // 允许访问静态资源或错误页面 (可选)
                        .requestMatchers("/error").permitAll()
                        //放行 Swagger 相关路径
                        .requestMatchers(
                                "/doc.html",              // 如果使用 knife4j
                                "/swagger-ui/**",         // swagger ui 静态资源
                                "/swagger-resources/**",  // swagger 资源
                                "/v3/api-docs/**"         // openapi json 数据
                        ).permitAll()
                        // 除上面外的所有请求，都需要携带 Token 才能访问
                        .anyRequest().authenticated()
                )

                // 2.4 添加 JWT 过滤器
                // 意思是在执行标准的 "用户名密码验证" 之前，先执行我们的 "JWT Token 验证"
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 【重点修改】异常处理配置
                .exceptionHandling(exceptions -> exceptions

                        // 1. 处理 401 (未登录/Token失效)
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 直接使用 ResultCode.UNAUTHORIZED (401)
                            sendErrorResponse(response, Result.error(ResultCode.UNAUTHORIZED));
                        })

                        // 2. 处理 403 (权限不足)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // 直接使用 ResultCode.FORBIDDEN (403)
                            sendErrorResponse(response, Result.error(ResultCode.FORBIDDEN));
                        })
                );


        return http.build();
    }


    /**
     * 【通用响应方法】
     * 接收你的 Result 对象，自动转换成 JSON 并写入响应
     */
    private void sendErrorResponse(HttpServletResponse response, Result<?> result) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        // 关键点：HTTP 状态码也要同步设置（例如 Result 里是 401，HTTP 状态码也得是 401）
        response.setStatus(result.getCode());

        // 使用 Jackson 把 Result 对象转成 JSON 字符串
        // 例如：{"code":401, "message":"未登录或Token已过期", "data":null}
        String json = objectMapper.writeValueAsString(result);

        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.flush();
    }

}