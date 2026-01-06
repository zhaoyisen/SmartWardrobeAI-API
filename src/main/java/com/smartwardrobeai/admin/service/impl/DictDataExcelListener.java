package com.smartwardrobeai.admin.service.impl;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartwardrobeai.admin.mapper.SysDictDataMapper;
import com.smartwardrobeai.admin.mapper.SysDictTypeMapper;
import com.smartwardrobeai.admin.model.entity.SysDictData;
import com.smartwardrobeai.admin.model.entity.SysDictType;
import com.smartwardrobeai.admin.model.vo.DictDataExcelVO;
import com.smartwardrobeai.admin.model.vo.DictDataImportResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DictDataExcelListener implements ReadListener<DictDataExcelVO> {

    /**
     * 每隔100条存储数据库，防止数据几万条数据在内存，容易OOM
     */
    private static final int BATCH_COUNT = 100;

    /**
     * 缓存字典类型信息，避免重复查询数据库
     */
    private final Map<String, SysDictType> dictTypeCache = new ConcurrentHashMap<>();

    /**
     * 缓存已存在的字典数据（字典类型编码 + 字典值 -> 字典数据ID）
     */
    private final Map<String, Long> existingDataCache = new ConcurrentHashMap<>();

    /**
     * 正在处理中的数据（用于防止同一批数据重复处理）
     */
    private final java.util.Set<String> processingKeys = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private final SysDictDataMapper dictDataMapper;
    private final SysDictTypeMapper dictTypeMapper;
    private final String duplicateStrategy;

    /**
     * 成功列表
     */
    private final List<DictDataImportResultVO.ImportSuccessItem> successList = new ArrayList<>();

    /**
     * 失败列表
     */
    private final List<DictDataImportResultVO.ImportFailItem> failList = new ArrayList<>();

    /**
     * 当前行号（从2开始，因为第1行是表头）
     */
    private int currentRowNum = 1;

    /**
     * 临时存储，用于批量插入（包含行号和Excel数据）
     */
    private static class CachedDataItem {
        SysDictData entity;
        DictDataExcelVO excelVO;
        int rowNum;

        CachedDataItem(SysDictData entity, DictDataExcelVO excelVO, int rowNum) {
            this.entity = entity;
            this.excelVO = excelVO;
            this.rowNum = rowNum;
        }
    }

    private List<CachedDataItem> cachedDataList = new ArrayList<>(BATCH_COUNT);

    public DictDataExcelListener(SysDictDataMapper dictDataMapper, 
                                 SysDictTypeMapper dictTypeMapper,
                                 String duplicateStrategy) {
        this.dictDataMapper = dictDataMapper;
        this.dictTypeMapper = dictTypeMapper;
        this.duplicateStrategy = duplicateStrategy;
    }

    /**
     * 这个每一条数据解析都会来调用
     */
    @Override
    public void invoke(DictDataExcelVO data, AnalysisContext context) {
        currentRowNum = context.readRowHolder().getRowIndex() + 1;
        log.debug("解析到一条数据:{}", data);

        // 验证数据
        String errorMsg = validateData(data);
        if (errorMsg != null) {
            addFailItem(data, errorMsg);
            return;
        }

        // 获取字典类型
        SysDictType dictType = getDictType(data.getDictType());
        if (dictType == null) {
            addFailItem(data, "字典类型不存在或已禁用");
            return;
        }

        // 检查是否重复
        String cacheKey = dictType.getDictType() + ":" + data.getDictValue();
        Long existingId = existingDataCache.get(cacheKey);
        
        if (existingId != null) {
            // 已存在
            if ("skip".equals(duplicateStrategy)) {
                addFailItem(data, "数据已存在，跳过");
                return;
            } else if ("update".equals(duplicateStrategy)) {
                // 更新现有记录
                updateExistingData(existingId, data, dictType);
                addSuccessItem(data, "更新");
                return;
            }
        } else {
            // 检查数据库中是否真的存在（可能缓存中没有）
            SysDictData existing = dictDataMapper.selectOne(new LambdaQueryWrapper<SysDictData>()
                    .eq(SysDictData::getDictType, dictType.getDictType())
                    .eq(SysDictData::getDictValue, data.getDictValue())
                    .last("LIMIT 1"));
            
            if (existing != null) {
                existingDataCache.put(cacheKey, existing.getId());
                if ("skip".equals(duplicateStrategy)) {
                    addFailItem(data, "数据已存在，跳过");
                    return;
                } else if ("update".equals(duplicateStrategy)) {
                    updateExistingData(existing.getId(), data, dictType);
                    addSuccessItem(data, "更新");
                    return;
                }
            }
        }

        // 检查是否正在处理中（防止同一批数据重复）
        if (processingKeys.contains(cacheKey)) {
            addFailItem(data, "数据重复，跳过");
            return;
        }

        // 新增数据
        SysDictData entity = convertToEntity(data, dictType);
        cachedDataList.add(new CachedDataItem(entity, data, currentRowNum));
        
        // 标记为正在处理中
        processingKeys.add(cacheKey);

        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            cachedDataList = new ArrayList<>(BATCH_COUNT);
        }
    }

    /**
     * 所有数据解析完成了 都会来调用
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        saveData();
        log.info("所有数据解析完成！");
    }

    /**
     * 验证数据
     */
    private String validateData(DictDataExcelVO data) {
        if (!StringUtils.hasText(data.getDictType())) {
            return "字典类型编码不能为空";
        }
        if (!StringUtils.hasText(data.getDictLabel())) {
            return "字典标签不能为空";
        }
        if (!StringUtils.hasText(data.getDictValue())) {
            return "字典值不能为空";
        }
        return null;
    }

    /**
     * 获取字典类型（带缓存）
     */
    private SysDictType getDictType(String dictTypeCode) {
        if (!StringUtils.hasText(dictTypeCode)) {
            return null;
        }
        
        return dictTypeCache.computeIfAbsent(dictTypeCode, code -> {
            SysDictType type = dictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictType>()
                    .eq(SysDictType::getDictType, code)
                    .last("LIMIT 1"));
            // 只缓存启用状态的字典类型
            if (type != null && type.getStatus() == 1) {
                return type;
            }
            return null;
        });
    }

    /**
     * 转换为实体
     */
    private SysDictData convertToEntity(DictDataExcelVO data, SysDictType dictType) {
        SysDictData entity = new SysDictData();
        entity.setDictTypeId(dictType.getId());
        entity.setDictType(dictType.getDictType());
        entity.setDictLabel(data.getDictLabel());
        entity.setDictValue(data.getDictValue());
        entity.setPromptText(data.getPromptText());
        entity.setRemark(data.getRemark());
        entity.setSort(data.getSort() != null ? data.getSort() : 0);
        entity.setStatus(data.getStatus() != null ? data.getStatus() : 1);
        return entity;
    }

    /**
     * 更新现有数据
     */
    private void updateExistingData(Long id, DictDataExcelVO data, SysDictType dictType) {
        SysDictData entity = new SysDictData();
        entity.setId(id);
        entity.setDictTypeId(dictType.getId());
        entity.setDictType(dictType.getDictType());
        entity.setDictLabel(data.getDictLabel());
        entity.setDictValue(data.getDictValue());
        entity.setPromptText(data.getPromptText());
        entity.setRemark(data.getRemark());
        if (data.getSort() != null) {
            entity.setSort(data.getSort());
        }
        if (data.getStatus() != null) {
            entity.setStatus(data.getStatus());
        }
        dictDataMapper.updateById(entity);
    }

    /**
     * 保存数据
     */
    private void saveData() {
        if (cachedDataList.isEmpty()) {
            return;
        }
        
        try {
            for (CachedDataItem item : cachedDataList) {
                dictDataMapper.insert(item.entity);
                // 更新缓存
                String cacheKey = item.entity.getDictType() + ":" + item.entity.getDictValue();
                existingDataCache.put(cacheKey, item.entity.getId());
                // 移除处理中标记
                processingKeys.remove(cacheKey);
                // 添加到成功列表（使用保存的行号）
                int savedRowNum = item.rowNum;
                currentRowNum = savedRowNum; // 临时设置行号，以便addSuccessItem使用
                addSuccessItem(item.excelVO, "新增");
            }
        } catch (Exception e) {
            log.error("批量保存数据失败", e);
            // 将这批数据标记为失败
            for (CachedDataItem item : cachedDataList) {
                String cacheKey = item.entity.getDictType() + ":" + item.entity.getDictValue();
                // 移除处理中标记
                processingKeys.remove(cacheKey);
                int savedRowNum = item.rowNum;
                currentRowNum = savedRowNum; // 临时设置行号，以便addFailItem使用
                addFailItem(item.excelVO, "保存失败: " + e.getMessage());
            }
        }
    }

    /**
     * 添加成功项
     */
    private void addSuccessItem(DictDataExcelVO data, String operation) {
        DictDataImportResultVO.ImportSuccessItem item = DictDataImportResultVO.ImportSuccessItem.builder()
                .rowNum(currentRowNum)
                .dictType(data.getDictType())
                .dictLabel(data.getDictLabel())
                .dictValue(data.getDictValue())
                .operation(operation)
                .build();
        successList.add(item);
    }

    /**
     * 添加失败项
     */
    private void addFailItem(DictDataExcelVO data, String errorMessage) {
        DictDataImportResultVO.ImportFailItem item = DictDataImportResultVO.ImportFailItem.builder()
                .rowNum(currentRowNum)
                .dictType(data.getDictType())
                .dictLabel(data.getDictLabel())
                .dictValue(data.getDictValue())
                .errorMessage(errorMessage)
                .build();
        failList.add(item);
    }

    /**
     * 获取导入结果
     */
    public DictDataImportResultVO getImportResult() {
        int totalCount = successList.size() + failList.size();
        return DictDataImportResultVO.builder()
                .totalCount(totalCount)
                .successCount(successList.size())
                .failCount(failList.size())
                .successList(successList)
                .failList(failList)
                .build();
    }
}

