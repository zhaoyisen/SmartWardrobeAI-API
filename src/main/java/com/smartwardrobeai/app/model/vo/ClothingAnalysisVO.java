package com.smartwardrobeai.app.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI 智能分析结果 VO
 * 用于 Step 1: 上传图片后返回给前端的预填信息
 */
@Schema(description = "AI 智能分析服饰结果")
public record ClothingAnalysisVO(

        @Schema(description = "原始图片ID (保存时需传回)", example = "1001")
        Long imageId,

        @Schema(description = "原始图片URL (用于回显)", example = "http://minio/bucket/xx.jpg")
        String imageUrl,

        @Schema(description = "AI去底图ID (可能为空)", example = "1002")
        Long maskImageId,

        @Schema(description = "AI去底图URL (可能为空)", example = "http://minio/bucket/mask_xx.png")
        String maskImageUrl,

        // === AI 识别出的属性 ===

        @Schema(description = "识别出的品类 (Category)", example = "T-shirt")
        String category,

        @Schema(description = "自动推断的部位 (Region字典值)", example = "TOP")
        String region,

        @Schema(description = "自动推断的建议层级", example = "2")
        Integer defaultLayer,

        @Schema(description = "识别出的主色调", example = "White")
        String color,

        @Schema(description = "识别出的季节", example = "Summer")
        String season,

        @Schema(description = "识别出的版型", example = "Regular")
        String fitType,

        @Schema(description = "识别出的视角", example = "Flat")
        String viewType
) {
}