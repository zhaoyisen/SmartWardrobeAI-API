package com.smartwardrobeai.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 负责 Token 的生成、解析和校验
 */
@Component
public class JwtUtil {

    // 从 application.yml 读取配置
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration; // 单位：毫秒

    /**
     * 生成 SecretKey 对象
     * 根据 HMAC-SHA 算法要求，密钥转换
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * 【核心方法】全参数生成 Token
     *
     * @param userId   用户ID
     * @param subject  用户名/账号
     * @param userType 用户类型 (ADMIN / APP)
     */
    public String generateToken(Long userId, String subject, String userType) {
        // 将 userType 放入 claims map 中
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", userType); // 🔥 关键：写入身份标识

        return Jwts.builder()
                .subject(subject)
                .claims(claims) // 注入所有自定义属性
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }


    /**
     * 生成 Token
     * 对应 AuthService 中的调用：jwtUtil.generateToken(user.getId(), account)
     *
     * @param userId  用户ID (存入 Claims)
     * @param subject 用户名/邮箱/手机号 (存入 Subject)
     * @return 加密后的 Token 字符串
     */
    public String generateToken(Long userId, String subject) {
        return generateToken(userId, subject, "APP");
    }

    /**
     * 【新增方法】供 Admin 端使用 (对应 Controller 中的调用)
     * 这里的 subject 暂时用 userId.toString() 代替，或者你可以修改 Controller 传 username 进来
     */
    public String createToken(Long userId, String subject, String userType) {
        // 为了方便，这里 subject 直接填 userId 的字符串形式
        return generateToken(userId, subject, userType);
    }


    /**
     * 解析 Token 获取 Claims (荷载)
     * 如果 Token 过期或被篡改，这里会抛出异常
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // 验证签名
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    /**
     * 验证 Token 是否有效
     * (在这个简单的版本中，只要能解析出来且没过期就算有效)
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 【新增】获取 Token 中的 UserId
     *
     * @param token 加密后的 Token
     * @return 用户ID (如果解析失败或过期，返回 null)
     */
    public Long getUserId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            // 对应 generateToken 里的 .claim("userId", userId)
            // JJWT 会自动识别 Long 类型，但为了保险建议显式指定类型
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            // 解析失败（过期、篡改、格式错误），返回 null，不让工具类直接炸掉
            return null;
        }
    }


    /**
     * 【新增】获取用户类型 (ADMIN / APP)
     * 给拦截器/过滤器使用
     */
    public String getUserType(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("type", String.class);
        } catch (Exception e) {
            return null;
        }
    }

}