package com.smartwardrobeai.admin.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.smartwardrobeai.admin.model.dto.AiModelQueryDTO;
import com.smartwardrobeai.admin.model.dto.AiModelSaveDTO;
import com.smartwardrobeai.admin.model.entity.SysAiModel;
import com.smartwardrobeai.admin.model.vo.AiModelVO;
import com.smartwardrobeai.common.model.entity.PageResult;

import java.util.List;
import java.util.Map;

public interface SysAIModelService extends IService<SysAiModel> {


    PageResult<AiModelVO> pageQuery(AiModelQueryDTO queryDTO);


    void saveModel(AiModelSaveDTO saveDTO);


    void updateModel(AiModelSaveDTO saveDTO);


    AiModelVO getDetail(Long id);


    void updateStatus(Long id, Integer status);


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

    /**
     * 手动刷新AI模型缓存
     * 清除所有App端AI模型缓存（app:ai-model:*），下次查询时会重新从数据库加载
     */
    void refreshCache();
}
