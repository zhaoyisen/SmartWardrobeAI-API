package com.smartwardrobeai.app.model.dto;

import com.smartwardrobeai.app.model.enums.RegionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 保存衣物提交参数 DTO (全字段版)
 * 当 id 为 null 时表示新增，不为 null 时表示编辑
 */
@Schema(description = "保存衣物请求参数（新增或编辑）")
public record ClothingCreateDTO(

        // ================= 0. 标识字段 =================
        @Schema(description = "衣物ID (编辑时必填，新增时为空)", example = "1")
        Long id,

        // ================= 1. 核心关联 =================
        @Schema(description = "原始图片ID (必填)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
        @NotNull(message = "必须包含图片ID")
        Long imageId,

        @Schema(description = "分割后图片ID", example = "1002")
        Long maskImageId,

        // ================= 2. 核心分类 (可覆盖系统默认值) =================

        @Schema(description = "品类 (T-shirt, Jeans...)", requiredMode = Schema.RequiredMode.REQUIRED, example = "T-shirt")
        @NotBlank(message = "品类不能为空")
        String category,

        @Schema(description = "部位 (TOP, BOTTOM...)", example = "TOP")
        // 修改为枚举类型，利用 Jackson 自动反序列化校验
        RegionEnum region,

        @Schema(description = "建议层级 (1-Inner, 2-Middle, 3-Outer)", example = "2")
        // 允许为空，若为空则由后端根据 category 推断
        Integer defaultLayer,

        // ================= 3. AI 识别属性 (可修改) =================

        @Schema(description = "主色调", requiredMode = Schema.RequiredMode.REQUIRED, example = "White")
        @NotBlank(message = "颜色不能为空")
        String color,

        @Schema(description = "季节", requiredMode = Schema.RequiredMode.REQUIRED, example = "Summer")
        @NotBlank(message = "季节不能为空")
        String season,

        @Schema(description = "版型 (Slim/Regular/Loose)", example = "Regular")
        @Size(max = 32, message = "版型描述过长")
        String fitType,

        @Schema(description = "视角 (Flat/Hanger/Model)", example = "Flat")
        @Size(max = 32, message = "视角描述过长")
        String viewType,

        // ================= 4. 用户补充信息 (管理属性) =================

        @Schema(description = "衣物名称 (可选，不填自动生成)", example = "我的白T恤")
        @Size(max = 64, message = "衣物名称不能超过64个字符")
        String name,

        @Schema(description = "货架号/收纳位置", example = "A-1-05")
        @Size(max = 32, message = "货架号不能超过32个字符")
        String shelfNo,

        @Schema(description = "购买价格", example = "99.90")
        @DecimalMin(value = "0.0", message = "价格不能为负数")
        BigDecimal price,

        @Schema(description = "购买日期", example = "2023-12-01")
        @PastOrPresent(message = "购买时间不能是未来")
        LocalDate purchaseDate,

        @Schema(description = "品牌", example = "Uniqlo")
        @Size(max = 64, message = "品牌名称过长")
        String brand,

        @Schema(description = "尺码", example = "L")
        @Size(max = 32, message = "尺码描述过长")
        String size,

        // ================= 5. 状态与统计 (初始设定) =================

        @Schema(description = "初始状态 (1:在柜, 2:洗衣, 3:借出, 0:丢弃)", defaultValue = "1", example = "1")
        Integer status,

        @Schema(description = "初始穿着次数 (用于录入旧衣物)", defaultValue = "0", example = "10")
        Integer wearCount
) {
}