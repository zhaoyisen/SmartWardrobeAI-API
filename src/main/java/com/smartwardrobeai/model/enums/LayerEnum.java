package com.smartwardrobeai.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 穿衣层级 (Z-Index / Layer Order)
 * <p>
 * 核心逻辑：
 * 1. 仅在同一个 Region (部位) 内进行排序。
 * 例如：TOP(上装) 内部，INNER(1) < MIDDLE(2) < OUTER(3)。
 * 2. 不同 Region 互不干扰。
 * 例如：SHOES(鞋) 不需要和 TOP(上装) 比层级。
 * 3. 这是一个"建议值" (Default Layer)，用户在试穿时可以手动调整。
 */
@Getter
public enum LayerEnum {

    /**
     * 贴身层 / 内衣层
     * 适用：内衣、背心、打底衫、秋衣
     * 渲染顺序：最先渲染 (被其他层覆盖)
     */
    INNER(1, "贴身/内搭"),

    /**
     * 中间层 / 常规层 (默认值)
     * 适用：T恤、衬衫、卫衣、连衣裙、牛仔裤
     * 渲染顺序：在 INNER 之上，OUTER 之下
     */
    MIDDLE(2, "常规/中层"),

    /**
     * 外套层 / 最外层
     * 适用：夹克、大衣、羽绒服、西装外套
     * 渲染顺序：覆盖在 INNER 和 MIDDLE 之上
     */
    OUTER(3, "外套/最外层"),

    /**
     * 顶层 / 配饰层
     * 适用：围巾、挎包、腰带 (通常覆盖在衣服最外面)
     * 注意：帽子(Hat)属于独立部位，不参与此排序
     */
    ACCESSORY(4, "配饰/顶层");

    @EnumValue // 标记存入数据库的值是这个 int (1, 2, 3, 4)
    private final int code;

    private final String desc;

    LayerEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}