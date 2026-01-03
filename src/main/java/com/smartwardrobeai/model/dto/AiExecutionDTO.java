package com.smartwardrobeai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * AI 执行参数 DTO (前端传参)
 * 用于 Step 1: 上传图片并分析接口
 */
@Schema(description = "AI 模型执行配置参数")
public record AiExecutionDTO(

        /**
         * 模型唯一标识 Key
         * 对应数据库表 sys_ai_model 中的 model_key 字段
         */
        @Schema(description = "选中的模型Key (必填)", requiredMode = Schema.RequiredMode.REQUIRED, example = "qwen3-vl-flash")
        @NotBlank(message = "必须指定 AI 模型")
        String modelKey,

        /**
         * 是否开启思考模式 (Thinking Mode)
         * 仅当所选模型支持时有效
         */
        @Schema(description = "是否开启思考模式", example = "false")
        Boolean enableThinking,

        /**
         * 思考预算 Token 数
         * 仅当 enableThinking=true 时有效
         * 必须是正整数
         */
        @Schema(description = "用户设定的思考预算Token数", example = "81920")
        @Min(value = 1, message = "思考预算必须大于0")
        Long thinkingBudget
) {
}