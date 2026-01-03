package com.smartwardrobeai.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true) // 开启链式调用，写代码更爽
@Schema(description = "后台登录成功返回结果")
public class AdminLoginVO {

    @Schema(description = "认证Token")
    private String token;

    @Schema(description = "管理员ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    // 如果以后有权限菜单，可以在这里加
    // private List<String> permissions;
}