package com.smartwardrobeai.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartwardrobeai.admin.mapper.SysDictDataMapper;
import com.smartwardrobeai.admin.mapper.SysDictTypeMapper;
import com.smartwardrobeai.admin.model.dto.DictDataQueryDTO;
import com.smartwardrobeai.admin.model.dto.DictDataSaveDTO;
import com.smartwardrobeai.admin.model.entity.SysDictData;
import com.smartwardrobeai.admin.model.entity.SysDictType;
import com.smartwardrobeai.admin.model.vo.DictDataVO;
import com.smartwardrobeai.admin.service.SysDictDataService;
import com.smartwardrobeai.admin.service.SysDictTypeService;
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
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    private final SysDictTypeMapper dictTypeMapper;

    @Override
    public PageResult<DictDataVO> pageQuery(DictDataQueryDTO queryDTO) {
        // 1. 获取分页对象
        Page<SysDictData> page = queryDTO.toMpPage("sort", true);

        // 2. 自动生成 QueryWrapper
        QueryWrapper<SysDictData> wrapper = QueryGenerator.generate(queryDTO);

        // 3. 执行查询
        this.page(page, wrapper);

        // 4. 转换VO
        return PageResult.of(page, DictDataVO.class);
    }

    @Override
    public void saveDictData(DictDataSaveDTO saveDTO) {
        // 1. 校验字典类型是否存在且启用
        SysDictType dictType = dictTypeMapper.selectById(saveDTO.getDictTypeId());
        if (dictType == null) {
            throw new BusinessException("字典类型不存在");
        }
        if (dictType.getStatus() == 0) {
            throw new BusinessException("字典类型已禁用，无法添加数据");
        }

        // 2. 转换为实体
        SysDictData entity = new SysDictData();
        BeanUtils.copyProperties(saveDTO, entity);

        // 3. 自动填充 dict_type 字段
        entity.setDictType(dictType.getDictType());

        // 4. 默认值填充
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }

        // 5. 保存
        this.save(entity);
    }

    @Override
    public void updateDictData(DictDataSaveDTO saveDTO) {
        if (saveDTO.getId() == null) {
            throw new IllegalArgumentException("ID不能为空");
        }

        // 1. 校验字典类型是否存在且启用
        SysDictType dictType = dictTypeMapper.selectById(saveDTO.getDictTypeId());
        if (dictType == null) {
            throw new BusinessException("字典类型不存在");
        }
        if (dictType.getStatus() == 0) {
            throw new BusinessException("字典类型已禁用，无法修改数据");
        }

        // 2. 转换为实体
        SysDictData entity = new SysDictData();
        BeanUtils.copyProperties(saveDTO, entity);

        // 3. 自动填充 dict_type 字段
        entity.setDictType(dictType.getDictType());

        // 4. 更新
        this.updateById(entity);
    }

    @Override
    public DictDataVO getDetail(Long id) {
        SysDictData entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("字典数据不存在");
        }
        return BeanUtil.toBean(entity, DictDataVO.class);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        this.update(new LambdaUpdateWrapper<SysDictData>()
                .eq(SysDictData::getId, id)
                .set(SysDictData::getStatus, status));
    }

    @Override
    public List<Map<String, String>> getListByDictType(String dictType) {
        // 1. 查询条件：指定字典类型编码，状态为1 (启用)，按 sort 升序排列
        List<SysDictData> list = this.list(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 1)
                .orderByAsc(SysDictData::getSort));

        // 2. 转换为前端下拉框需要的结构
        return list.stream().map(item -> {
            Map<String, String> map = new HashMap<>();
            map.put("value", item.getDictValue());      // 字典值
            map.put("label", item.getDictLabel());      // 字典标签
            map.put("promptText", item.getPromptText()); // AI提示词补充
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, String>> getListByDictTypeId(Long dictTypeId) {
        // 1. 查询条件：指定字典类型ID，状态为1 (启用)，按 sort 升序排列
        List<SysDictData> list = this.list(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictTypeId, dictTypeId)
                .eq(SysDictData::getStatus, 1)
                .orderByAsc(SysDictData::getSort));

        // 2. 转换为前端下拉框需要的结构
        return list.stream().map(item -> {
            Map<String, String> map = new HashMap<>();
            map.put("value", item.getDictValue());      // 字典值
            map.put("label", item.getDictLabel());      // 字典标签
            map.put("promptText", item.getPromptText()); // AI提示词补充
            return map;
        }).collect(Collectors.toList());
    }
}

