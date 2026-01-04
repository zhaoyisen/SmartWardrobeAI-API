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
@Schema(description = "字典数据实体类")
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 字典类型ID（关联sys_dict_type.id）
     */
    private Long dictTypeId;

    /**
     * 字典类型编码（冗余字段，方便查询）
     */
    private String dictType;

    /**
     * 字典标签（显示文本，如：男、女、红色）
     */
    private String dictLabel;

    /**
     * 字典值（存储值，如：male, female, red）
     */
    private String dictValue;

    /**
     * AI提示词补充（关键字段，用于AI识别，如：dark red, burgundy）
     */
    private String promptText;

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

