package com.smartwardrobeai.app.model.vo;

import com.smartwardrobeai.app.model.enums.RegionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 衣物展示对象
 * 用于返回给前端，包含所有字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "衣物信息")
public class ClothingVO {

    @Schema(description = "衣物ID")
    private Long id;

    @Schema(description = "所属用户ID")
    private Long userId;

    // ================= 核心图片区 =================

    @Schema(description = "原始图片ID")
    private Long imageId;

    @Schema(description = "原始图片URL")
    private String imageUrl;

    @Schema(description = "AI抠图后的透明底图ID")
    private Long maskImageId;

    @Schema(description = "AI抠图后的透明底图URL")
    private String maskImageUrl;

    // ================= 核心分类 =================

    @Schema(description = "衣物名称")
    private String name;

    @Schema(description = "部位 (TOP, BOTTOM, DRESS, SHOES, ACCESSORY)")
    private RegionEnum region;

    @Schema(description = "具体品类")
    private String category;

    @Schema(description = "建议层级 (1:Inner, 2:Middle, 3:Outer, 4:Accessory)")
    private Integer defaultLayer;

    // ================= AI 识别属性 =================

    @Schema(description = "主色调")
    private String color;

    @Schema(description = "适用季节")
    private String season;

    @Schema(description = "版型")
    private String fitType;

    @Schema(description = "视角")
    private String viewType;

    // ================= 用户补充信息 =================

    @Schema(description = "货架号/收纳位置")
    private String shelfNo;

    @Schema(description = "品牌")
    private String brand;

    @Schema(description = "尺码")
    private String size;

    @Schema(description = "购买价格")
    private BigDecimal price;

    @Schema(description = "购买日期")
    private LocalDate purchaseDate;

    // ================= 状态与统计 =================

    @Schema(description = "状态 (1:在柜, 2:洗衣中, 3:借出, 0:丢弃)")
    private Integer status;

    @Schema(description = "穿着次数")
    private Integer wearCount;

    // ================= 系统字段 =================

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

