package com.smartwardrobeai.admin.controller;

import com.smartwardrobeai.admin.model.dto.DictDataImportDTO;
import com.smartwardrobeai.admin.model.dto.DictDataQueryDTO;
import com.smartwardrobeai.admin.model.dto.DictDataSaveDTO;
import com.smartwardrobeai.admin.model.vo.DictDataImportResultVO;
import com.smartwardrobeai.admin.model.vo.DictDataVO;
import com.smartwardrobeai.admin.service.SysDictDataService;
import com.smartwardrobeai.common.Result;
import com.smartwardrobeai.common.model.entity.PageResult;
import com.smartwardrobeai.common.validation.NotEmptyFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dict-data")
@Tag(name = "【后台】字典数据管理")
@RequiredArgsConstructor
@Validated
public class DictDataAdminController {

    private final SysDictDataService dictDataService;

    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public Result<PageResult<DictDataVO>> page(DictDataQueryDTO queryDTO) {
        return Result.success(dictDataService.pageQuery(queryDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取详情")
    public Result<DictDataVO> getDetail(@PathVariable Long id) {
        return Result.success(dictDataService.getDetail(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增字典数据")
    public Result<Void> add(@RequestBody @Valid DictDataSaveDTO saveDTO) {
        dictDataService.saveDictData(saveDTO);
        return Result.success(null);
    }

    @PutMapping("/update")
    @Operation(summary = "修改字典数据")
    public Result<Void> update(@RequestBody @Valid DictDataSaveDTO saveDTO) {
        dictDataService.updateDictData(saveDTO);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除字典数据")
    public Result<Void> delete(@PathVariable Long id) {
        dictDataService.removeById(id);
        return Result.success(null);
    }

    @PatchMapping("/status/{id}/{status}")
    @Operation(summary = "修改状态", description = "1启用 0禁用")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        dictDataService.updateStatus(id, status);
        return Result.success(null);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        dictDataService.removeBatchByIds(ids);
        return Result.success(null);
    }

    @GetMapping("/list-by-type/{dictType}")
    @Operation(summary = "根据字典类型编码获取启用的字典数据列表", description = "用于前端下拉框，包含promptText")
    public Result<List<Map<String, String>>> getListByDictType(@PathVariable String dictType) {
        return Result.success(dictDataService.getListByDictType(dictType));
    }

    @GetMapping("/list-by-type-id/{dictTypeId}")
    @Operation(summary = "根据字典类型ID获取启用的字典数据列表", description = "用于前端下拉框，包含promptText")
    public Result<List<Map<String, String>>> getListByDictTypeId(@PathVariable Long dictTypeId) {
        return Result.success(dictDataService.getListByDictTypeId(dictTypeId));
    }

    @PostMapping("/import")
    @Operation(summary = "导入字典数据", description = "从Excel文件批量导入字典数据，支持skip(跳过重复)和update(更新重复)两种策略", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)))
    public Result<DictDataImportResultVO> importData(@RequestParam("file") @NotEmptyFile(message = "文件不能为空") MultipartFile file, @RequestParam(value = "duplicateStrategy", defaultValue = "skip") String duplicateStrategy) {
        DictDataImportDTO importDTO = new com.smartwardrobeai.admin.model.dto.DictDataImportDTO();
        importDTO.setFile(file);
        importDTO.setDuplicateStrategy(duplicateStrategy);
        DictDataImportResultVO result = dictDataService.importFromExcel(importDTO);
        return Result.success(result);
    }

    @GetMapping("/template")
    @Operation(summary = "下载导入模板", description = "下载Excel导入模板文件，包含示例数据")
    public void downloadTemplate(HttpServletResponse response) {
        dictDataService.downloadTemplate(response);
    }

    @PostMapping("/refresh-cache")
    @Operation(summary = "手动刷新字典缓存", description = "清除所有App端字典缓存（app:dict:type:*），下次查询时会重新从数据库加载")
    public Result<Void> refreshCache() {
        dictDataService.refreshCache();
        return Result.success(null);
    }
}

