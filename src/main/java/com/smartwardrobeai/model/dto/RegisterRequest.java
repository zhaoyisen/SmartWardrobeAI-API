package com.smartwardrobeai.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 注册请求 DTO
 * 使用 Java record 定义不可变数据载体
 */
public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        String username,

        String email,   // 可选，但在 Service 层会校验 email 和 phone 至少有一个
        String phone,   // 可选

        @NotBlank(message = "密码不能为空")
        String password
) {
}



