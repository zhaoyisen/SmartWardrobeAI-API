package com.smartwardrobeai.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartwardrobeai.admin.model.dto.DictDataImportDTO;
import com.smartwardrobeai.admin.model.dto.DictDataQueryDTO;
import com.smartwardrobeai.admin.model.dto.DictDataSaveDTO;
import com.smartwardrobeai.admin.model.entity.SysDictData;
import com.smartwardrobeai.admin.model.vo.DictDataImportResultVO;
import com.smartwardrobeai.admin.model.vo.DictDataVO;
import com.smartwardrobeai.common.model.entity.PageResult;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

public interface SysDictDataService extends IService<SysDictData> {

    /**
     * 分页查询字典数据列表
     */
    PageResult<DictDataVO> pageQuery(DictDataQueryDTO queryDTO);

    /**
     * 新增字典数据
     */
    void saveDictData(DictDataSaveDTO saveDTO);

    /**
     * 修改字典数据
     */
    void updateDictData(DictDataSaveDTO saveDTO);

    /**
     * 获取字典数据详情
     */
    DictDataVO getDetail(Long id);

    /**
     * 修改字典数据状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 根据字典类型编码获取启用的字典数据列表（用于前端下拉框）
     *
     * @param dictType 字典类型编码（如：clothing_color）
     * @return 包含 value(dictValue), label(dictLabel), promptText 的列表
     */
    List<Map<String, String>> getListByDictType(String dictType);

    /**
     * 根据字典类型ID获取启用的字典数据列表（用于前端下拉框）
     *
     * @param dictTypeId 字典类型ID
     * @return 包含 value(dictValue), label(dictLabel), promptText 的列表
     */
    List<Map<String, String>> getListByDictTypeId(Long dictTypeId);

    /**
     * 从Excel文件导入字典数据
     *
     * @param importDTO 导入请求DTO
     * @return 导入结果
     */
    DictDataImportResultVO importFromExcel(DictDataImportDTO importDTO);

    /**
     * 下载导入模板
     *
     * @param response HTTP响应
     */
    void downloadTemplate(HttpServletResponse response);
}

