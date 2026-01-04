package com.smartwardrobeai.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "【后台】字典类型 新增/修改 表单")
public class DictTypeSaveDTO {

    @Schema(description = "ID (修改时必填，新增时忽略)")
    private Long id;

    @Schema(description = "字典类型编码（唯一标识，如：gender）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典类型编码不能为空")
    private String dictType;

    @Schema(description = "字典类型名称（如：性别）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典类型名称不能为空")
    private String dictName;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "排序值")
    private Integer sort;

    @Schema(description = "状态 (1启用 0禁用)")
    private Integer status;
}

