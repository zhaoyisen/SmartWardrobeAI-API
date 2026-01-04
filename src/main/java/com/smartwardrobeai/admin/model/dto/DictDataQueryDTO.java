package com.smartwardrobeai.admin.model.dto;

import com.smartwardrobeai.common.annotation.Query;
import com.smartwardrobeai.common.annotation.QueryType;
import com.smartwardrobeai.common.model.entity.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "【后台】字典数据分页查询参数")
public class DictDataQueryDTO extends BasePageQuery {

    @Schema(description = "字典类型ID")
    @Query(type = QueryType.EQ)
    private Long dictTypeId;

    @Schema(description = "字典类型编码")
    @Query(type = QueryType.EQ)
    private String dictType;

    @Schema(description = "字典标签")
    @Query(type = QueryType.LIKE, blurry = "dict_label")
    private String dictLabel;

    @Schema(description = "状态 (1启用 0禁用)")
    @Query(type = QueryType.EQ)
    private Integer status;
}

