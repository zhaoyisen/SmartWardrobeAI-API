package com.smartwardrobeai.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.smartwardrobeai.model.entity.SysAiModel;

import java.util.List;
import java.util.Map;

public interface SysAIModelService extends IService<SysAiModel> {


    /**
     * 获取可用的 AI 模型下拉列表
     *
     * @return 包含 value(modelKey) 和 label(模型名称) 的列表
     */
    List<Map<String, String>> getDropdownList();


    /**
     * 根据 Key 获取模型详情 (会对 API Key 进行脱敏处理)
     *
     * @param modelKey 前端传递的模型唯一标识
     * @return 模型实体
     */
    SysAiModel getModelDetail(String modelKey);
}
