package com.smartwardrobeai.admin.model.dto;

import com.smartwardrobeai.common.annotation.Query;
import com.smartwardrobeai.common.annotation.QueryType;
import com.smartwardrobeai.common.model.entity.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "【后台】AI模型分页查询参数")
public class AiModelQueryDTO extends BasePageQuery { // 🔥 继承基类

    // pageNum, pageSize 都不用写了，父类有

    @Schema(description = "模型key")
    @Query(type = QueryType.LIKE, blurry = "model_key")
    private String modelKey;

    @Schema(description = "模型展示名称")
    @Query(type = QueryType.LIKE, blurry = "label")
    private String label;

    @Schema(description = "状态 (1启用 0禁用)")
    @Query(type = QueryType.EQ)
    private Integer status;
}