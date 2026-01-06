package com.smartwardrobeai.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "【后台】品类策略列表展示对象")
public class CategoryStrategyVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "品类代码")
    private String categoryCode;

    @Schema(description = "中文描述")
    private String categoryDesc;

    @Schema(description = "部位字典值")
    private String region;

    @Schema(description = "部位字典标签（用于前端显示）")
    private String regionLabel;

    @Schema(description = "层级字典值")
    private String layer;

    @Schema(description = "层级字典标签（用于前端显示）")
    private String layerLabel;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

