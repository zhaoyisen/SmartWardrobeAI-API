package com.smartwardrobeai.app.service;

import com.smartwardrobeai.app.model.vo.CategoryVO;

import java.util.List;

/**
 * App端品类服务接口
 * 提供品类查询功能，包含Redis缓存机制
 */
public interface CategoryService {

    /**
     * 获取所有启用的品类列表（带缓存）
     * 按sort排序
     *
     * @return 品类VO列表，包含核心字段
     */
    List<CategoryVO> getCategoryList();
}

