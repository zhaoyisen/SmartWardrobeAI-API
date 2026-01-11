package com.smartwardrobeai.app.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * App端AI模型展示对象
 * 用于返回给前端，不包含敏感信息（如apiKey）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "App端AI模型信息")
public class AiModelVO {

    @Schema(description = "模型Key（前端传参标识）", example = "qwen-plus")
    private String modelKey;

    @Schema(description = "模型展示名称", example = "通义千问VL Plus")
    private String label;

    @Schema(description = "底层模型ID（API调用名称）", example = "qwen-vl-plus")
    private String modelName;

    @Schema(description = "接口Base URL", example = "https://dashscope.aliyuncs.com/compatible-mode/v1")
    private String baseUrl;

    @Schema(description = "是否支持思考模式", example = "true")
    private Boolean supportThinking;

    @Schema(description = "最大允许的思考Token数", example = "4096")
    private Long maxThinkingBudget;

    @Schema(description = "默认是否开启思考模式", example = "false")
    private Boolean defaultEnableThinking;

    @Schema(description = "默认思考Token预算", example = "1024")
    private Long defaultThinkingBudget;

    @Schema(description = "排序值", example = "1")
    private Integer sort;
}

