package com.smartwardrobeai.app.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 *
 * 发送验证码请求 (通用)
 */
@Schema(description = "发送验证码请求")
public record SendCodeRequest(
        @Schema(description = "接收目标(手机号或邮箱)", example = "13800138000")
        @NotBlank
        String target,

        @Schema(description = "类型: sms 或 email", example = "sms")
        @NotBlank
        String type
) {
}