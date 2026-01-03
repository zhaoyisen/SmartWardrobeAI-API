package com.smartwardrobeai.model.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 自动分类策略
 * 用于根据具体的品类 (Category) 自动推断 部位 (Region) 和 层级 (Layer)
 */
@Getter
public enum CategoryStrategy {

    // ================= 上装 (TOP) =================
    T_SHIRT("T-shirt", "T恤", RegionEnum.TOP, LayerEnum.MIDDLE),
    SHIRT("Shirt", "衬衫", RegionEnum.TOP, LayerEnum.MIDDLE),
    HOODIE("Hoodie", "卫衣", RegionEnum.TOP, LayerEnum.MIDDLE),
    SWEATER("Sweater", "毛衣/针织衫", RegionEnum.TOP, LayerEnum.MIDDLE),
    VEST("Vest", "背心/马甲", RegionEnum.TOP, LayerEnum.INNER), // 马甲有时也可以是Outer，这里默认Inner或Middle，视业务而定

    // 外套 (TOP - OUTER)
    JACKET("Jacket", "夹克", RegionEnum.TOP, LayerEnum.OUTER),
    COAT("Coat", "大衣/风衣", RegionEnum.TOP, LayerEnum.OUTER),
    BLAZER("Blazer", "西装外套", RegionEnum.TOP, LayerEnum.OUTER),
    DOWN_JACKET("DownJacket", "羽绒服", RegionEnum.TOP, LayerEnum.OUTER),

    // ================= 下装 (BOTTOM) =================
    JEANS("Jeans", "牛仔裤", RegionEnum.BOTTOM, LayerEnum.MIDDLE),
    PANTS("Pants", "休闲裤/西裤", RegionEnum.BOTTOM, LayerEnum.MIDDLE),
    SHORTS("Shorts", "短裤", RegionEnum.BOTTOM, LayerEnum.MIDDLE),
    SKIRT("Skirt", "半身裙", RegionEnum.BOTTOM, LayerEnum.MIDDLE),

    // ================= 全身 (DRESS) =================
    DRESS("Dress", "连衣裙", RegionEnum.DRESS, LayerEnum.MIDDLE),
    JUMPSUIT("Jumpsuit", "连体裤", RegionEnum.DRESS, LayerEnum.MIDDLE),

    // ================= 鞋履 (SHOES) =================
    SNEAKERS("Sneakers", "运动鞋", RegionEnum.SHOES, LayerEnum.MIDDLE), // 鞋子层级无所谓，默认MIDDLE
    BOOTS("Boots", "靴子", RegionEnum.SHOES, LayerEnum.MIDDLE),
    SANDALS("Sandals", "凉鞋", RegionEnum.SHOES, LayerEnum.MIDDLE),
    HEELS("Heels", "高跟鞋", RegionEnum.SHOES, LayerEnum.MIDDLE),

    // ================= 配饰 (ACCESSORY) =================
    HAT("Hat", "帽子", RegionEnum.ACCESSORY, LayerEnum.ACCESSORY),
    SCARF("Scarf", "围巾", RegionEnum.ACCESSORY, LayerEnum.ACCESSORY),
    BAG("Bag", "包袋", RegionEnum.ACCESSORY, LayerEnum.ACCESSORY),

    // ================= 兜底默认值 =================
    UNKNOWN("Unknown", "未知", RegionEnum.TOP, LayerEnum.MIDDLE);

    private final String code;       // 存入数据库的英文值 (前端传这个)
    private final String desc;       // 中文描述 (给前端展示用)
    private final RegionEnum region; // 自动推断的部位
    private final LayerEnum layer;   // 自动推断的层级

    CategoryStrategy(String code, String desc, RegionEnum region, LayerEnum layer) {
        this.code = code;
        this.desc = desc;
        this.region = region;
        this.layer = layer;
    }

    /**
     * 核心方法：根据前端传来的 String code 查找策略
     */
    public static CategoryStrategy match(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code)) // 忽略大小写
                .findFirst()
                .orElse(UNKNOWN);
    }
}