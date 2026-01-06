package com.smartwardrobeai.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "【后台】品类策略 新增/修改 表单")
public class CategoryStrategySaveDTO {

    @Schema(description = "ID (修改时必填，新增时忽略)")
    private Long id;

    @Schema(description = "品类代码（唯一，如 T-shirt）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "品类代码不能为空")
    private String categoryCode;

    @Schema(description = "中文描述（如 T恤）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "中文描述不能为空")
    private String categoryDesc;

    @Schema(description = "部位字典值（如 TOP）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "部位不能为空")
    private String region;

    @Schema(description = "层级字典值（如 MIDDLE）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "层级不能为空")
    private String layer;

    @Schema(description = "排序值")
    private Integer sort;

    @Schema(description = "状态 (1启用 0禁用)")
    private Integer status;

    @Schema(description = "备注说明")
    private String remark;
}

