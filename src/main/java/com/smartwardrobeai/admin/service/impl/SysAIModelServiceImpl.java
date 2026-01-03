package com.smartwardrobeai.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartwardrobeai.admin.mapper.SysAiModelMapper;
import com.smartwardrobeai.admin.model.dto.AiModelQueryDTO;
import com.smartwardrobeai.admin.model.dto.AiModelSaveDTO;
import com.smartwardrobeai.admin.model.entity.SysAiModel;
import com.smartwardrobeai.admin.model.vo.AiModelVO;
import com.smartwardrobeai.admin.service.SysAIModelService;
import com.smartwardrobeai.common.BusinessException;
import com.smartwardrobeai.common.model.entity.PageResult;
import com.smartwardrobeai.utils.QueryGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
    public PageResult<AiModelVO> pageQuery(AiModelQueryDTO queryDTO) {
        // 1. 获取分页对象 (自动处理了 current, size, sort)
        Page<SysAiModel> page = queryDTO.toMpPage("sort", false);


        // 2. 🔥 核心：一行代码自动生成 QueryWrapper
        // 它会自动读取 DTO 上的 @Query 注解，生成 like(label, val) 或 eq(status, val) 等 SQL
        QueryWrapper<SysAiModel> wrapper = QueryGenerator.generate(queryDTO);


        // 3. 执行查询
        this.page(page, wrapper);

        //转换vo
        return PageResult.of(page, AiModelVO.class);


    }

    @Override
    public void saveModel(AiModelSaveDTO saveDTO) {
        // 查重
        Long count = this.lambdaQuery().eq(SysAiModel::getModelKey, saveDTO.getModelKey()).count();
        if (count > 0) {
            throw new RuntimeException("模型Key [" + saveDTO.getModelKey() + "] 已存在");
        }

        SysAiModel entity = new SysAiModel();
        BeanUtils.copyProperties(saveDTO, entity);

        // 默认值填充
        if (entity.getStatus() == null) entity.setStatus(1);
        if (entity.getSort() == null) entity.setSort(0);

        this.save(entity);
    }

    @Override
    public void updateModel(AiModelSaveDTO saveDTO) {
        if (saveDTO.getId() == null) throw new IllegalArgumentException("ID不能为空");

        SysAiModel updateEntity = new SysAiModel();
        BeanUtils.copyProperties(saveDTO, updateEntity);

        this.updateById(updateEntity);
    }

    @Override
    public AiModelVO getDetail(Long id) {
        return BeanUtil.toBean(getById(id), AiModelVO.class);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        this.update(new LambdaUpdateWrapper<SysAiModel>()
                .eq(SysAiModel::getId, id)
                .set(SysAiModel::getStatus, status));
    }


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
