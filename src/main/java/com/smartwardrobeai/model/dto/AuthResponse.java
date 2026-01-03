package com.smartwardrobeai.model.dto;

/**
 * 认证响应 DTO
 * 用于登录或注册成功后，向前端返回的关键信息
 */
public record AuthResponse(
        String token,    // JWT 令牌 (前端需存储在 localStorage)
        Long userId,     // 用户ID (方便前端记录当前是谁)
        String username  // 用户名 (用于页面展示欢迎语)
) {}