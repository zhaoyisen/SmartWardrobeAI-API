package com.smartwardrobeai.admin.model.dto;

import com.smartwardrobeai.common.annotation.Query;
import com.smartwardrobeai.common.annotation.QueryType;
import com.smartwardrobeai.common.model.entity.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "【后台】品类策略分页查询参数")
public class CategoryStrategyQueryDTO extends BasePageQuery {

    @Schema(description = "品类代码")
    @Query(type = QueryType.LIKE, blurry = "category_code")
    private String categoryCode;

    @Schema(description = "中文描述")
    @Query(type = QueryType.LIKE, blurry = "category_desc")
    private String categoryDesc;

    @Schema(description = "部位字典值")
    @Query(type = QueryType.EQ)
    private String region;

    @Schema(description = "层级字典值")
    @Query(type = QueryType.EQ)
    private String layer;

    @Schema(description = "状态 (1启用 0禁用)")
    @Query(type = QueryType.EQ)
    private Integer status;
}

