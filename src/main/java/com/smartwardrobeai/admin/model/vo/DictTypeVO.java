package com.smartwardrobeai.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "【后台】字典类型列表展示对象")
public class DictTypeVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "字典类型编码")
    private String dictType;

    @Schema(description = "字典类型名称")
    private String dictName;

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

