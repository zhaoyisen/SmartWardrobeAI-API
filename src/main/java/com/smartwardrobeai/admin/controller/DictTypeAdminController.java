package com.smartwardrobeai.admin.controller;

import com.smartwardrobeai.admin.model.dto.DictTypeQueryDTO;
import com.smartwardrobeai.admin.model.dto.DictTypeSaveDTO;
import com.smartwardrobeai.admin.model.vo.DictTypeVO;
import com.smartwardrobeai.admin.service.SysDictTypeService;
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
@RequestMapping("/api/admin/dict-type")
@Tag(name = "【后台】字典类型管理")
@RequiredArgsConstructor
public class DictTypeAdminController {

    private final SysDictTypeService dictTypeService;

    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public Result<PageResult<DictTypeVO>> page(DictTypeQueryDTO queryDTO) {
        return Result.success(dictTypeService.pageQuery(queryDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取详情")
    public Result<DictTypeVO> getDetail(@PathVariable Long id) {
        return Result.success(dictTypeService.getDetail(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增字典类型")
    public Result<Void> add(@RequestBody @Valid DictTypeSaveDTO saveDTO) {
        dictTypeService.saveDictType(saveDTO);
        return Result.success(null);
    }

    @PutMapping("/update")
    @Operation(summary = "修改字典类型")
    public Result<Void> update(@RequestBody @Valid DictTypeSaveDTO saveDTO) {
        dictTypeService.updateDictType(saveDTO);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除字典类型")
    public Result<Void> delete(@PathVariable Long id) {
        dictTypeService.removeDictTypeById(id);
        return Result.success(null);
    }

    @PatchMapping("/status/{id}/{status}")
    @Operation(summary = "修改状态", description = "1启用 0禁用")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        dictTypeService.updateStatus(id, status);
        return Result.success(null);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        dictTypeService.removeDictTypeBatchByIds(ids);
        return Result.success(null);
    }

    @GetMapping("/dropdown")
    @Operation(summary = "获取所有启用的字典类型下拉列表")
    public Result<List<Map<String, String>>> getDropdownList() {
        return Result.success(dictTypeService.getDropdownList());
    }
}

