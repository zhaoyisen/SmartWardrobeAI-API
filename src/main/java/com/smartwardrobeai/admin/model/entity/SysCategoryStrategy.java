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
@Schema(description = "品类策略配置实体类")
@TableName("sys_category_strategy")
public class SysCategoryStrategy extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 品类代码（唯一，如 "T-shirt"）
     */
    private String categoryCode;

    /**
     * 中文描述（如 "T恤"）
     */
    private String categoryDesc;

    /**
     * 部位字典值（关联 sys_dict_data.dict_value，如 "TOP"）
     */
    private String region;

    /**
     * 层级字典值（关联 sys_dict_data.dict_value，如 "MIDDLE"）
     */
    private String layer;

    /**
     * 排序值
     */
    private Integer sort;

    /**
     * 状态：1启用 0禁用
     */
    private Integer status;

    /**
     * 备注说明
     */
    private String remark;
}

