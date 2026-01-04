package com.smartwardrobeai.admin.model.dto;

import com.smartwardrobeai.common.annotation.Query;
import com.smartwardrobeai.common.annotation.QueryType;
import com.smartwardrobeai.common.model.entity.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "【后台】字典类型分页查询参数")
public class DictTypeQueryDTO extends BasePageQuery {

    @Schema(description = "字典类型编码")
    @Query(type = QueryType.LIKE, blurry = "dict_type")
    private String dictType;

    @Schema(description = "字典类型名称")
    @Query(type = QueryType.LIKE, blurry = "dict_name")
    private String dictName;

    @Schema(description = "状态 (1启用 0禁用)")
    @Query(type = QueryType.EQ)
    private Integer status;
}

