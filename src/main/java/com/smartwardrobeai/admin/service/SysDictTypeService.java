package com.smartwardrobeai.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartwardrobeai.admin.model.dto.DictTypeQueryDTO;
import com.smartwardrobeai.admin.model.dto.DictTypeSaveDTO;
import com.smartwardrobeai.admin.model.entity.SysDictType;
import com.smartwardrobeai.admin.model.vo.DictTypeVO;
import com.smartwardrobeai.common.model.entity.PageResult;

import java.util.List;
import java.util.Map;

public interface SysDictTypeService extends IService<SysDictType> {

    /**
     * 分页查询字典类型列表
     */
    PageResult<DictTypeVO> pageQuery(DictTypeQueryDTO queryDTO);

    /**
     * 新增字典类型
     */
    void saveDictType(DictTypeSaveDTO saveDTO);

    /**
     * 修改字典类型
     */
    void updateDictType(DictTypeSaveDTO saveDTO);

    /**
     * 获取字典类型详情
     */
    DictTypeVO getDetail(Long id);

    /**
     * 修改字典类型状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 删除字典类型（会检查是否有关联的字典数据）
     */
    void removeDictTypeById(Long id);

    /**
     * 批量删除字典类型（会检查是否有关联的字典数据）
     */
    void removeDictTypeBatchByIds(List<Long> ids);

    /**
     * 获取所有启用的字典类型下拉列表
     *
     * @return 包含 value(dictType) 和 label(dictName) 的列表
     */
    List<Map<String, String>> getDropdownList();
}

