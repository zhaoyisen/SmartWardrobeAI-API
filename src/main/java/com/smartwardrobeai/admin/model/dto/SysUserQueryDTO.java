package com.smartwardrobeai.admin.model.dto;

import com.smartwardrobeai.common.annotation.Query;
import com.smartwardrobeai.common.annotation.QueryType;
import com.smartwardrobeai.common.model.entity.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "【后台】管理端用户分页查询参数")
public class SysUserQueryDTO extends BasePageQuery {

    @Schema(description = "用户名")
    @Query(type = QueryType.LIKE, blurry = "username")
    private String username;

    @Schema(description = "昵称")
    @Query(type = QueryType.LIKE, blurry = "nickname")
    private String nickname;

    @Schema(description = "状态 (1启用 0禁用)")
    @Query(type = QueryType.EQ)
    private Integer status;
}

