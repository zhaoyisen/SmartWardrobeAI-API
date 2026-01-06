package com.smartwardrobeai.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 最近衣物VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "最近新增的衣物")
public class RecentClothingVO {

    @Schema(description = "衣物ID")
    private Long id;

    @Schema(description = "衣物名称")
    private String name;

    @Schema(description = "部位")
    private String region;

    @Schema(description = "品类")
    private String category;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

