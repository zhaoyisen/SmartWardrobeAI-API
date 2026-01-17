package com.smartwardrobeai.app.model.dto;

import com.smartwardrobeai.app.model.enums.RegionEnum;
import com.smartwardrobeai.common.annotation.Query;
import com.smartwardrobeai.common.annotation.QueryType;
import com.smartwardrobeai.common.model.entity.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 衣物查询参数DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "衣物查询参数")
public class ClothingQueryDTO extends BasePageQuery {

    @Schema(description = "部位筛选 (TOP, BOTTOM, DRESS, SHOES, ACCESSORY)，为空表示查询全部", example = "TOP")
    @Query(type = QueryType.EQ, column = "region")
    private RegionEnum region;

    @Schema(description = "品类筛选，为空表示查询全部", example = "T-shirt")
    @Query(type = QueryType.EQ, column = "category")
    private String category;

    @Schema(description = "层级筛选 (1:Inner, 2:Middle, 3:Outer, 4:Accessory)，为空表示查询全部", example = "2")
    @Query(type = QueryType.EQ, column = "default_layer")
    private Integer defaultLayer;

    @Schema(description = "颜色筛选，为空表示查询全部", example = "White")
    @Query(type = QueryType.EQ, column = "color")
    private String color;

    @Schema(description = "季节筛选，为空表示查询全部", example = "Spring")
    @Query(type = QueryType.EQ, column = "season")
    private String season;

    @Schema(description = "版型筛选，为空表示查询全部", example = "Regular")
    @Query(type = QueryType.EQ, column = "fit_type")
    private String fitType;
}

