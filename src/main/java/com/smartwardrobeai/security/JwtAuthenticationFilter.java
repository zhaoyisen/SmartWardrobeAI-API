package com.smartwardrobeai.security;

import com.smartwardrobeai.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * 作用：拦截所有 HTTP 请求，检查 Header 中是否包含有效的 Authorization Token。
 * 如果包含且有效，则将用户信息注入到 Spring Security 的上下文中，
 * 这样后续的 Controller 才能知道“当前是谁在操作”。
 */
@Component
@RequiredArgsConstructor // 使用 Lombok 自动生成构造函数，注入 JwtUtil
@Slf4j // 只有调试时才需要日志，可选
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * 过滤器的核心逻辑
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 尝试从请求头中获取 Token
        // 前端约定格式 -> Authorization: Bearer <token_string>
        String authHeader = request.getHeader("Authorization");

        // 2. 如果没有 Header 或者格式不对，直接放行
        // (注意：这里放行意味着该请求将以“匿名用户”身份继续访问，
        // 如果该接口需要在 SecurityConfig 中配置为 authenticated，则会被后续的拦截器拒绝)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 提取纯净的 Token 字符串 (去掉 "Bearer " 前缀)
        String token = authHeader.substring(7);

        try {
            // 4. 校验 Token 是否有效 (签名正确且未过期)
            if (jwtUtil.validateToken(token)) {

                // 5. 解析 Token，获取内部存储的信息 (Claims)
                Claims claims = jwtUtil.extractAllClaims(token);
                String username = claims.getSubject();          // 获取用户名/账号
                Long userId = claims.get("userId", Long.class); // 获取我们存入的 userId

                // 6. 如果用户名存在，且当前上下文 (Context) 中还没有认证信息
                // (getAuthentication() == null 说明还没登录过)
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // 7. 构建 Spring Security 的标准认证对象
                    // 参数1: Principal (通常放 User 对象或 ID，Controller 可以直接拿)
                    // 参数2: Credentials (密码，这里已认证通过，不需要放密码，传 null)
                    // 参数3: Authorities (权限列表，这里暂时给个默认 ROLE_USER)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );

                    // 8. 设置请求的详细信息 (IP, SessionID 等，虽然 JWT 无状态不太用 SessionID)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 9. 【最关键一步】将认证对象塞入 SecurityContext
                    // 只要执行了这一步，后续的 Controller 用 SecurityContextHolder.getContext().getAuthentication() 就能拿到用户信息了
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token 过期或无效时，不抛出异常，只是不设置认证信息。
            // 这样请求会以“匿名”身份继续，如果访问的是保护接口，Security 会自动返回 403 Forbidden。
            log.warn("JWT 认证失败: {}", e.getMessage());
        }

        // 10. 继续执行过滤器链中的下一个过滤器
        filterChain.doFilter(request, response);
    }
}