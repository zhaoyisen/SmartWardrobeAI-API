package com.smartwardrobeai.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "【后台】管理端用户 新增/修改 表单")
public class SysUserSaveDTO {

    @Schema(description = "ID (修改时必填，新增时忽略)")
    private Long id;

    @Schema(description = "用户名（唯一）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码（新增时必填，修改时可选，为空则不更新密码）")
    private String password;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "状态 (1启用 0禁用)")
    private Integer status;
}

