package com.smartwardrobeai.admin.controller;

import com.smartwardrobeai.admin.model.dto.CategoryStrategyQueryDTO;
import com.smartwardrobeai.admin.model.dto.CategoryStrategySaveDTO;
import com.smartwardrobeai.admin.model.vo.CategoryStrategyVO;
import com.smartwardrobeai.admin.service.SysCategoryStrategyService;
import com.smartwardrobeai.common.Result;
import com.smartwardrobeai.common.model.entity.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/category-strategy")
@Tag(name = "【后台】品类策略配置")
@RequiredArgsConstructor
public class CategoryStrategyAdminController {

    private final SysCategoryStrategyService categoryStrategyService;

    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public Result<PageResult<CategoryStrategyVO>> page(CategoryStrategyQueryDTO queryDTO) {
        return Result.success(categoryStrategyService.pageQuery(queryDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取详情")
    public Result<CategoryStrategyVO> getDetail(@PathVariable Long id) {
        return Result.success(categoryStrategyService.getDetail(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增策略")
    public Result<Void> add(@RequestBody @Valid CategoryStrategySaveDTO saveDTO) {
        categoryStrategyService.saveStrategy(saveDTO);
        return Result.success(null);
    }

    @PutMapping("/update")
    @Operation(summary = "修改策略")
    public Result<Void> update(@RequestBody @Valid CategoryStrategySaveDTO saveDTO) {
        categoryStrategyService.updateStrategy(saveDTO);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除策略")
    public Result<Void> delete(@PathVariable Long id) {
        categoryStrategyService.removeById(id);
        return Result.success(null);
    }

    @PatchMapping("/status/{id}/{status}")
    @Operation(summary = "修改状态", description = "1启用 0禁用")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        categoryStrategyService.updateStatus(id, status);
        return Result.success(null);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        categoryStrategyService.removeBatchByIds(ids);
        return Result.success(null);
    }

    @PostMapping("/refresh-cache")
    @Operation(summary = "手动刷新缓存", description = "清除所有品类策略相关的缓存，下次查询时会重新从数据库加载")
    public Result<Void> refreshCache() {
        categoryStrategyService.refreshCache();
        return Result.success(null);
    }
}

