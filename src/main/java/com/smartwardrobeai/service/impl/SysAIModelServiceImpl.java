package com.smartwardrobeai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartwardrobeai.common.BusinessException;
import com.smartwardrobeai.mapper.SysAiModelMapper;
import com.smartwardrobeai.model.entity.SysAiModel;
import com.smartwardrobeai.service.SysAIModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class SysAIModelServiceImpl extends ServiceImpl<SysAiModelMapper, SysAiModel> implements SysAIModelService {


    @Override
    public List<Map<String, String>> getDropdownList() {
        // 1. 查询条件：状态为1 (启用)，按 sort 升序排列
        List<SysAiModel> list = this.list(new LambdaQueryWrapper<SysAiModel>().eq(SysAiModel::getStatus, 1).orderByAsc(SysAiModel::getSort));

        // 2. 转换为前端下拉框需要的简易结构 [{value: "qwen-plus", label: "通义千问Plus"}, ...]
        return list.stream().map(item -> {
            Map<String, String> map = new HashMap<>();
            map.put("value", item.getModelKey()); // 传给后端的唯一标识
            map.put("label", item.getLabel());    // 展示给用户的名称
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public SysAiModel getModelDetail(String modelKey) {
        // 1. 根据 modelKey 查询唯一记录
        SysAiModel model = this.getOne(new LambdaQueryWrapper<SysAiModel>().eq(SysAiModel::getModelKey, modelKey));

        // 2. 安全处理：即使查到了，也必须把 API Key 抹除
        // 这是一个非常重要的业务逻辑，防止密钥泄露给前端
        if (model != null) {
            model.setApiKey(null);
        } else {
            log.error("前端请求了不存在的模型配置: {}", modelKey);
            throw new BusinessException("模型不存在");
        }

        return model;
    }
}
