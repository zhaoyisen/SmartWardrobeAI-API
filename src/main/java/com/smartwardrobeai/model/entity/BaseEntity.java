package com.smartwardrobeai.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseEntity implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 创建时间
     * FieldFill.INSERT: MP 在执行 insert 操作时会自动填充此字段 (需配置 MetaObjectHandler)
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")

    private LocalDateTime createTime;


    /**
     * 更新时间
     * FieldFill.INSERT_UPDATE: MP 在执行 insert 或 update 操作时都会更新此字段
     */
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}