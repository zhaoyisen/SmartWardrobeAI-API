package com.smartwardrobeai.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "【后台】AI模型 新增/修改 表单")
public class AiModelSaveDTO {

    @Schema(description = "ID (修改时必填，新增时忽略)")
    private Long id;

    @Schema(description = "模型Key (前端传参标识, 如 qwen-plus)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模型Key不能为空")
    private String modelKey;

    @Schema(description = "前端展示名称 (如 通义千问Plus)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "展示名称不能为空")
    private String label;

    @Schema(description = "底层调用模型ID (如 qwen-vl-plus)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "调用模型ID不能为空")
    private String modelName;

    @Schema(description = "API Base URL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "API地址不能为空")
    private String baseUrl;

    @Schema(description = "API Key (修改时若为空则不更新)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "API Key不能为空")
    private String apiKey;

    @Schema(description = "是否支持思考模式")
    @NotNull(message = "必须指定是否支持思考模式")
    private Boolean supportThinking;

    @Schema(description = "最大思考Token预算 (风控)")
    private Long maxThinkingBudget;

    @Schema(description = "默认是否开启思考")
    private Boolean defaultEnableThinking;

    @Schema(description = "默认思考Token预算")
    private Long defaultThinkingBudget;

    @Schema(description = "排序值")
    private Integer sort;

    @Schema(description = "状态 (1启用 0禁用)")
    private Integer status;
}