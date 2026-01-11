package com.smartwardrobeai.app.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * App端品类展示对象
 * 用于返回给前端，包含核心字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "App端品类信息")
public class CategoryVO {

    @Schema(description = "品类代码", example = "T-shirt")
    private String categoryCode;

    @Schema(description = "中文描述", example = "T恤")
    private String categoryDesc;

    @Schema(description = "部位字典值", example = "TOP")
    private String region;

    @Schema(description = "层级字典值", example = "MIDDLE")
    private String layer;

    @Schema(description = "排序值", example = "1")
    private Integer sort;
}

