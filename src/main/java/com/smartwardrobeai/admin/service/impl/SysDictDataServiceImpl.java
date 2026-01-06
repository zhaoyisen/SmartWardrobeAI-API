package com.smartwardrobeai.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartwardrobeai.admin.mapper.SysDictDataMapper;
import com.smartwardrobeai.admin.mapper.SysDictTypeMapper;
import com.smartwardrobeai.admin.model.dto.DictDataImportDTO;
import com.smartwardrobeai.admin.model.dto.DictDataQueryDTO;
import com.smartwardrobeai.admin.model.dto.DictDataSaveDTO;
import com.smartwardrobeai.admin.model.entity.SysDictData;
import com.smartwardrobeai.admin.model.entity.SysDictType;
import com.smartwardrobeai.admin.model.vo.DictDataExcelVO;
import com.smartwardrobeai.admin.model.vo.DictDataImportResultVO;
import com.smartwardrobeai.admin.model.vo.DictDataVO;
import com.smartwardrobeai.admin.service.SysDictDataService;
import com.smartwardrobeai.admin.service.SysDictTypeService;
import com.smartwardrobeai.common.BusinessException;
import com.smartwardrobeai.common.model.entity.PageResult;
import com.smartwardrobeai.utils.QueryGenerator;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

    @Override
    public DictDataImportResultVO importFromExcel(DictDataImportDTO importDTO) {
        MultipartFile file = importDTO.getFile();
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 验证文件格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || 
            (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            throw new BusinessException("文件格式不正确，仅支持 .xlsx 或 .xls 格式");
        }

        String duplicateStrategy = StringUtils.hasText(importDTO.getDuplicateStrategy()) 
                ? importDTO.getDuplicateStrategy() 
                : "skip";

        if (!"skip".equals(duplicateStrategy) && !"update".equals(duplicateStrategy)) {
            throw new BusinessException("重复数据处理策略参数错误，仅支持 skip 或 update");
        }

        try {
            // 创建监听器
            DictDataExcelListener listener = new DictDataExcelListener(
                    this.baseMapper, 
                    dictTypeMapper, 
                    duplicateStrategy
            );

            // 读取Excel文件
            EasyExcel.read(file.getInputStream(), DictDataExcelVO.class, listener)
                    .sheet()
                    .doRead();

            // 返回导入结果
            return listener.getImportResult();
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("导入字典数据失败", e);
            throw new BusinessException("导入字典数据失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("字典数据导入模板", StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 创建示例数据
            List<DictDataExcelVO> templateData = new ArrayList<>();
            DictDataExcelVO example1 = new DictDataExcelVO();
            example1.setDictType("clothing_color");
            example1.setDictLabel("红色");
            example1.setDictValue("red");
            example1.setPromptText("dark red, crimson, scarlet");
            example1.setRemark("基础颜色");
            example1.setSort(1);
            example1.setStatus(1);
            templateData.add(example1);

            DictDataExcelVO example2 = new DictDataExcelVO();
            example2.setDictType("clothing_color");
            example2.setDictLabel("蓝色");
            example2.setDictValue("blue");
            example2.setPromptText("navy blue, sky blue");
            example2.setRemark("基础颜色");
            example2.setSort(2);
            example2.setStatus(1);
            templateData.add(example2);

            // 写入Excel
            EasyExcel.write(response.getOutputStream(), DictDataExcelVO.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("字典数据")
                    .doWrite(templateData);
        } catch (IOException e) {
            log.error("下载模板失败", e);
            throw new BusinessException("下载模板失败: " + e.getMessage());
        }
    }
}

