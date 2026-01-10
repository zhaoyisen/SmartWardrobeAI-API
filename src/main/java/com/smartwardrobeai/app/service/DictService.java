package com.smartwardrobeai.app.service;

import java.util.List;
import java.util.Map;

/**
 * App端字典服务接口
 * 提供字典数据查询功能，包含Redis缓存机制
 */
public interface DictService {

    /**
     * 根据字典类型编码获取启用的字典数据列表
     *
     * @param dictType 字典类型编码（如：clothing_color）
     * @return 包含 value(dictValue), label(dictLabel), promptText 的列表
     */
    List<Map<String, String>> getDictByType(String dictType);

    /**
     * 批量获取多个字典类型的数据
     *
     * @param dictTypes 字典类型编码列表（如：["clothing_color", "gender"]）
     * @return Map结构，key为字典类型编码，value为字典数据列表
     */
    Map<String, List<Map<String, String>>> getDictBatch(List<String> dictTypes);
}

