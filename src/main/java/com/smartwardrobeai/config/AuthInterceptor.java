package com.smartwardrobeai.config;

import com.smartwardrobeai.common.UserContext;
import com.smartwardrobeai.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求过来后获取其中的userId
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 如果不是映射到方法直接通过 (比如跨域请求的 OPTIONS)
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        // 1. 获取 Token (通常在 Header 的 Authorization 字段，格式 "Bearer xxxxx")
        String token = request.getHeader("Authorization");
        // 2. 如果有 Token，解析并存入 UserContext
        if (StringUtils.isNotBlank(token)) {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtils.getUserId(token);
            if (userId != null) {
                UserContext.setUserId(userId);
            }
        }
        // 3. 关键点：无论有没有 Token，都返回 true (放行)！
        // 因为如果这个接口需要登录，Spring Security 在前面那关早就把它拦下来了。
        // 能走到这里的，要么是已经登录的，要么是 Security 允许匿名访问的（比如登录接口）。
        // 所以拦截器不需要当坏人，只负责“如果有ID就存一下”即可。
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 4. 请求结束，清理 ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
}