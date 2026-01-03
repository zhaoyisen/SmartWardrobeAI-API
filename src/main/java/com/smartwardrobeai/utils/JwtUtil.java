package com.smartwardrobeai.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

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
     * 生成 Token
     * 对应 AuthService 中的调用：jwtUtil.generateToken(user.getId(), account)
     *
     * @param userId  用户ID (存入 Claims)
     * @param subject 用户名/邮箱/手机号 (存入 Subject)
     * @return 加密后的 Token 字符串
     */
    public String generateToken(Long userId, String subject) {
        return Jwts.builder()
                .subject(subject)              // 设置主题 (通常是账号)
                .claim("userId", userId)       // 设置自定义荷载 (用户ID)
                .issuedAt(new Date())          // 签发时间
                .expiration(new Date(System.currentTimeMillis() + expiration)) // 过期时间
                .signWith(getSigningKey())     // 签名
                .compact();
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

}