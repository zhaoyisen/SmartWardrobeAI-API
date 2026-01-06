package com.smartwardrobeai.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 趋势数据VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "趋势数据")
public class TrendDataVO {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "日期字符串（用于前端展示）")
    private String dateStr;

    @Schema(description = "新增衣物数")
    private Long clothingCount;

    @Schema(description = "新增用户数")
    private Long userCount;

    @Schema(description = "新增文件数")
    private Long fileCount;
}

