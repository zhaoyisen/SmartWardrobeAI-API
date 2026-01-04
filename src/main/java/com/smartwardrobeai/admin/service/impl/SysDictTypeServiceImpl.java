package com.smartwardrobeai.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartwardrobeai.admin.mapper.SysDictTypeMapper;
import com.smartwardrobeai.admin.model.dto.DictTypeQueryDTO;
import com.smartwardrobeai.admin.model.dto.DictTypeSaveDTO;
import com.smartwardrobeai.admin.model.entity.SysDictData;
import com.smartwardrobeai.admin.model.entity.SysDictType;
import com.smartwardrobeai.admin.model.vo.DictTypeVO;
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
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    private final SysDictDataService dictDataService;

    @Override
    public PageResult<DictTypeVO> pageQuery(DictTypeQueryDTO queryDTO) {
        // 1. 获取分页对象
        Page<SysDictType> page = queryDTO.toMpPage("sort", true);

        // 2. 自动生成 QueryWrapper
        QueryWrapper<SysDictType> wrapper = QueryGenerator.generate(queryDTO);

        // 3. 执行查询
        this.page(page, wrapper);

        // 4. 转换VO
        return PageResult.of(page, DictTypeVO.class);
    }

    @Override
    public void saveDictType(DictTypeSaveDTO saveDTO) {
        // 1. 查重：检查 dict_type 编码是否已存在
        Long count = this.lambdaQuery()
                .eq(SysDictType::getDictType, saveDTO.getDictType())
                .count();
        if (count > 0) {
            throw new BusinessException("字典类型编码 [" + saveDTO.getDictType() + "] 已存在");
        }

        // 2. 转换为实体
        SysDictType entity = new SysDictType();
        BeanUtils.copyProperties(saveDTO, entity);

        // 3. 默认值填充
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }

        // 4. 保存
        this.save(entity);
    }

    @Override
    public void updateDictType(DictTypeSaveDTO saveDTO) {
        if (saveDTO.getId() == null) {
            throw new IllegalArgumentException("ID不能为空");
        }

        // 1. 查重：检查 dict_type 编码是否已存在（排除自身）
        Long count = this.lambdaQuery()
                .eq(SysDictType::getDictType, saveDTO.getDictType())
                .ne(SysDictType::getId, saveDTO.getId())
                .count();
        if (count > 0) {
            throw new BusinessException("字典类型编码 [" + saveDTO.getDictType() + "] 已存在");
        }

        // 2. 转换为实体
        SysDictType entity = new SysDictType();
        BeanUtils.copyProperties(saveDTO, entity);

        // 3. 更新
        this.updateById(entity);
    }

    @Override
    public DictTypeVO getDetail(Long id) {
        SysDictType entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("字典类型不存在");
        }
        return BeanUtil.toBean(entity, DictTypeVO.class);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        this.update(new LambdaUpdateWrapper<SysDictType>()
                .eq(SysDictType::getId, id)
                .set(SysDictType::getStatus, status));
    }

    @Override
    public void removeDictTypeById(Long id) {
        // 检查是否有关联的字典数据
        Long count = dictDataService.lambdaQuery()
                .eq(SysDictData::getDictTypeId, id)
                .count();
        if (count > 0) {
            throw new BusinessException("请先删除该类型下的所有字典数据");
        }
        // 删除字典类型
        this.removeById(id);
    }

    @Override
    public void removeDictTypeBatchByIds(List<Long> ids) {
        // 检查每个ID是否有关联的字典数据
        for (Long id : ids) {
            Long count = dictDataService.lambdaQuery()
                    .eq(SysDictData::getDictTypeId, id)
                    .count();
            if (count > 0) {
                SysDictType dictType = this.getById(id);
                String dictName = dictType != null ? dictType.getDictName() : String.valueOf(id);
                throw new BusinessException("字典类型 [" + dictName + "] 下存在字典数据，请先删除");
            }
        }
        // 批量删除字典类型
        this.removeBatchByIds(ids);
    }

    @Override
    public List<Map<String, String>> getDropdownList() {
        // 1. 查询条件：状态为1 (启用)，按 sort 升序排列
        List<SysDictType> list = this.list(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getStatus, 1)
                .orderByAsc(SysDictType::getSort));

        // 2. 转换为前端下拉框需要的简易结构
        return list.stream().map(item -> {
            Map<String, String> map = new HashMap<>();
            map.put("value", item.getDictType()); // 字典类型编码
            map.put("label", item.getDictName()); // 字典类型名称
            return map;
        }).collect(Collectors.toList());
    }
}

