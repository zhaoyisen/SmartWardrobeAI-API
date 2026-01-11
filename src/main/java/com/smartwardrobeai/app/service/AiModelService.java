package com.smartwardrobeai.app.service;

import com.smartwardrobeai.app.model.vo.AiModelVO;

import java.util.List;

/**
 * App端AI模型服务接口
 * 提供AI模型查询功能，包含Redis缓存机制
 */
public interface AiModelService {

    /**
     * 获取所有启用状态的AI模型列表（完整信息）
     * 包含思考模式配置等信息，按sort排序
     *
     * @return AI模型VO列表，不包含敏感信息（apiKey等）
     */
    List<AiModelVO> getModelList();

    /**
     * 根据modelKey获取AI模型详情
     * 包含思考模式配置等信息
     *
     * @param modelKey 模型唯一标识Key
     * @return AI模型VO，不包含敏感信息（apiKey等）
     * @throws com.smartwardrobeai.common.BusinessException 当模型不存在时抛出
     */
    AiModelVO getModelDetail(String modelKey);
}

