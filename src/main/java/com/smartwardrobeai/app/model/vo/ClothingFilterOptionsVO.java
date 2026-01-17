package com.smartwardrobeai.app.model.vo;

import com.smartwardrobeai.app.model.enums.RegionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 衣物筛选选项VO
 * 用于返回用户当前衣物已有的筛选条件选项，供前端展示查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "衣物筛选选项")
public class ClothingFilterOptionsVO {

    @Schema(description = "用户已有的部位列表")
    private List<RegionEnum> regions;

    @Schema(description = "用户已有的品类列表")
    private List<String> categories;

    @Schema(description = "用户已有的层级列表")
    private List<Integer> layers;

    @Schema(description = "用户已有的颜色列表")
    private List<String> colors;

    @Schema(description = "用户已有的季节列表")
    private List<String> seasons;

    @Schema(description = "用户已有的版型列表")
    private List<String> fitTypes;
}

