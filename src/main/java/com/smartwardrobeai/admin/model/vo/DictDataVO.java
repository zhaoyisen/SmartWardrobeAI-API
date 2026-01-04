package com.smartwardrobeai.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "【后台】字典数据列表展示对象")
public class DictDataVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "字典类型ID")
    private Long dictTypeId;

    @Schema(description = "字典类型编码")
    private String dictType;

    @Schema(description = "字典标签")
    private String dictLabel;

    @Schema(description = "字典值")
    private String dictValue;

    @Schema(description = "AI提示词补充")
    private String promptText;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "排序值")
    private Integer sort;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

