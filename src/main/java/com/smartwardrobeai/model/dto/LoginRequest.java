package com.smartwardrobeai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求 DTO
 */
@Schema(description = "账号密码登录请求")
public record LoginRequest(
        @Schema(description = "账号(手机或邮箱)", example = "13800138000")
        @NotBlank
        String account,

        @Schema(description = "密码", example = "123456")
        @NotBlank
        String password
) {
}


