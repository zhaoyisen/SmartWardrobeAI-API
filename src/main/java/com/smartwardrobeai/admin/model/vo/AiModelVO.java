package com.smartwardrobeai.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "【后台】AI模型列表展示对象")
public class AiModelVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "模型Key")
    private String modelKey;

    @Schema(description = "展示名称")
    private String label;

    @Schema(description = "底层模型ID")
    private String modelName;

    @Schema(description = "Base URL")
    private String baseUrl;

    @Schema(description = "API Key (已脱敏)")
    private String apiKey; // 前端展示为 sk-******

    @Schema(description = "是否支持思考模式")
    private Boolean supportThinking;

    @Schema(description = "最大思考Token")
    private Long maxThinkingBudget;

    @Schema(description = "默认开启思考")
    private Boolean defaultEnableThinking;

    @Schema(description = "默认Token预算")
    private Long defaultThinkingBudget;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "备注")
    private String remark;
}