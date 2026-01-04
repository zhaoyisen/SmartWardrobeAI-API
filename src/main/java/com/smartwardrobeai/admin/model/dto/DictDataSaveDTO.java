package com.smartwardrobeai.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "【后台】字典数据 新增/修改 表单")
public class DictDataSaveDTO {

    @Schema(description = "ID (修改时必填，新增时忽略)")
    private Long id;

    @Schema(description = "字典类型ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "字典类型ID不能为空")
    private Long dictTypeId;

    @Schema(description = "字典标签（显示文本，如：酒红）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;

    @Schema(description = "字典值（存储值，如：burgundy）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典值不能为空")
    private String dictValue;

    @Schema(description = "AI提示词补充（如：dark red, burgundy, wine red）")
    private String promptText;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "排序值")
    private Integer sort;

    @Schema(description = "状态 (1启用 0禁用)")
    private Integer status;
}

