package com.smartwardrobeai.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum RegionEnum {
    TOP("TOP", "上装"),
    BOTTOM("BOTTOM", "下装"),
    DRESS("DRESS", "连体/全身"),
    SHOES("SHOES", "鞋履"),
    ACCESSORY("ACCESSORY", "配饰");

    @EnumValue // 标记这个字段的值存入数据库
    private final String code;
    private final String desc;

    RegionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}