package com.smartwardrobeai.admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartwardrobeai.app.model.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字典类型实体类")
@TableName("sys_dict_type")
public class SysDictType extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 字典类型编码（唯一标识，如：gender, status, color）
     */
    private String dictType;

    /**
     * 字典类型名称（如：性别、状态、颜色）
     */
    private String dictName;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 排序值（数字越小越靠前）
     */
    private Integer sort;

    /**
     * 状态：1启用 0禁用
     */
    private Integer status;
}

