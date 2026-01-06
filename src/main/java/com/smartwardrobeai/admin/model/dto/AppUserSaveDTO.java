package com.smartwardrobeai.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "【后台】App端用户 修改 表单")
public class AppUserSaveDTO {
    @Schema(description = "ID (修改时必填)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "用户昵称")
    private String username;

    @Schema(description = "邮箱（唯一，登录凭证）")
    private String email;

    @Schema(description = "手机号（唯一，登录凭证）")
    private String phone;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "身高(cm)")
    private Integer height;

    @Schema(description = "体重(kg)")
    private Integer weight;

    @Schema(description = "状态 (1启用 0禁用)")
    private Integer status;
}

