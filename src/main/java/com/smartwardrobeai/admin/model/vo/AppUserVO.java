package com.smartwardrobeai.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "【后台】App端用户列表展示对象")
public class AppUserVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户昵称")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "身高(cm)")
    private Integer height;

    @Schema(description = "体重(kg)")
    private Integer weight;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

