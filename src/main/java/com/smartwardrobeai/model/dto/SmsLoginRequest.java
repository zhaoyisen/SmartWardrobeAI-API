package com.smartwardrobeai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 手机短信登录/注册请求
 */
@Schema(description = "短信登录/注册请求参数")
public record SmsLoginRequest(
        @Schema(description = "手机号", example = "13800138000")
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        @Schema(description = "短信验证码", example = "888888")
        @NotBlank(message = "验证码不能为空")
        String verifyCode
) {
}