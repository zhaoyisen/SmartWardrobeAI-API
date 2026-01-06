package com.smartwardrobeai.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分类统计")
public class CategoryStatsVO {

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类值")
    private String value;

    @Schema(description = "数量")
    private Long count;

    @Schema(description = "占比（百分比）")
    private Double percentage;
}

