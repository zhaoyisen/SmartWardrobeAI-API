package com.smartwardrobeai.admin.controller;

import com.smartwardrobeai.admin.model.dto.DictDataQueryDTO;
import com.smartwardrobeai.admin.model.dto.DictDataSaveDTO;
import com.smartwardrobeai.admin.model.vo.DictDataVO;
import com.smartwardrobeai.admin.service.SysDictDataService;
import com.smartwardrobeai.common.Result;
import com.smartwardrobeai.common.model.entity.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dict-data")
@Tag(name = "【后台】字典数据管理")
@RequiredArgsConstructor
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
}

