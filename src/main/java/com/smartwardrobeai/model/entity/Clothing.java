package com.smartwardrobeai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartwardrobeai.model.enums.RegionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 智能衣物实体类
 * 对应数据库表: clothing
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("clothing")
@Schema(description = "智能衣物实体类")
public class Clothing extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    // ================= 核心图片区 =================

    /**
     * 原始图片ID (关联 sys_file)
     */
    private Long imageId;

    /**
     * AI抠图后的透明底图ID (用于试穿，可为空)
     */
    private Long maskImageId;

    // ================= 核心分类 =================

    /**
     * 衣物名称 (例如: 我的白T恤)
     */
    private String name;

    /**
     * 部位 (枚举: TOP, BOTTOM, DRESS, SHOES, ACCESSORY)
     * 对应数据库字段: region
     */
    private RegionEnum region;

    /**
     * 具体品类 (例如: T-shirt, Jeans, Coat)
     * 对应数据库字段: category
     */
    private String category;

    /**
     * 建议层级 (1:Inner, 2:Middle, 3:Outer, 4:Accessory)
     * 对应 LayerEnum 的 code
     */
    private Integer defaultLayer;

    // ================= AI 识别属性 =================

    /**
     * 主色调 (例如: White, Blue)
     */
    private String color;

    /**
     * 适用季节 (Spring, Summer...)
     */
    private String season;

    /**
     * 版型 (Slim, Regular, Loose, Oversize)
     */
    private String fitType;

    /**
     * 视角 (Flat, Hanger, Model, Folded)
     */
    private String viewType;

    // ================= 用户补充信息 =================

    /**
     * 货架号/收纳位置
     */
    private String shelfNo;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 尺码 (S/M/L, 40/42)
     */
    private String size;

    /**
     * 购买价格
     */
    private BigDecimal price;

    /**
     * 购买日期
     */
    private LocalDate purchaseDate;

    // ================= 状态与统计 =================

    /**
     * 状态 (1:在柜, 2:洗衣中, 3:借出, 0:丢弃)
     */
    private Integer status;

    /**
     * 穿着次数
     */
    private Integer wearCount;

    /**
     * 逻辑删除 (0:正常, 1:删除)
     */
    @TableLogic
    private Integer delFlag;
}