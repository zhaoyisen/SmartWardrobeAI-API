package com.smartwardrobeai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 邮箱注册请求体
 */
@Schema(description = "邮箱注册请求参数")
public record EmailRegisterRequest(
        @Schema(description = "用户名", example = "fashion_user")
        @NotBlank(message = "用户名不能为空")
        String username,

        @Schema(description = "邮箱", example = "test@example.com")
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email,

        @Schema(description = "密码", example = "123456")
        @NotBlank(message = "密码不能为空")
        String password,

        @Schema(description = "验证码", example = "123456")
        @NotBlank(message = "验证码不能为空")
        String verifyCode
) {
}