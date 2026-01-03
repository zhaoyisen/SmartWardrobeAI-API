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
@Schema(description = "智能衣物实体类")
@TableName("sys_ai_model")
public class SysAiModel extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模型key
     */
    private String modelKey;

    /**
     * 模型前端展示名字
     */
    private String label;
    /**
     * api调用名字
     */
    private String modelName;
    /**
     * 调用地址
     */
    private String baseUrl;
    /**
     * apikey
     */
    private String apiKey;


    /**
     * 能力开关: 是否支持思考模式  1支持，0不支持
     */
    private Boolean supportThinking;

    /**
     * 风控限制: 最大允许的思考Token数
     */
    private Long maxThinkingBudget;

    /**
     * 默认配置: 若前端未传，是否默认开启   1开启，0不开启
     */
    private Boolean defaultEnableThinking;

    /**
     * 默认配置: 若前端未传，默认Token数
     */
    private Long defaultThinkingBudget;

    // ====================

    private Integer sort;
    /**
     * 状态: 1启用 0禁用
     */
    private Integer status;
}