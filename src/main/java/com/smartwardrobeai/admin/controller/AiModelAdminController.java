package com.smartwardrobeai.admin.controller;

import com.smartwardrobeai.admin.model.dto.AiModelQueryDTO;
import com.smartwardrobeai.admin.model.dto.AiModelSaveDTO;
import com.smartwardrobeai.admin.model.vo.AiModelVO;
import com.smartwardrobeai.admin.service.SysAIModelService;
import com.smartwardrobeai.common.Result;
import com.smartwardrobeai.common.model.entity.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ai-model")
@Tag(name = "【后台】AI模型配置")
@RequiredArgsConstructor
public class AiModelAdminController {

    private final SysAIModelService sysAiModelService;

    @GetMapping("/page")
    @Operation(summary = "分页查询")
    // 参数直接用对象接收，SpringMVC 会自动封装 QueryString 参数
    public Result<PageResult<AiModelVO>> page(AiModelQueryDTO queryDTO) {
        return Result.success(sysAiModelService.pageQuery(queryDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取详情")
    public Result<AiModelVO> getDetail(@PathVariable Long id) {
        return Result.success(sysAiModelService.getDetail(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增模型")
    public Result<Void> add(@RequestBody @Valid AiModelSaveDTO saveDTO) {
        sysAiModelService.saveModel(saveDTO);
        return Result.success(null);
    }

    @PutMapping("/update")
    @Operation(summary = "修改模型")
    public Result<Void> update(@RequestBody @Valid AiModelSaveDTO saveDTO) {
        sysAiModelService.updateModel(saveDTO);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模型")
    public Result<Void> delete(@PathVariable Long id) {
        sysAiModelService.removeById(id);
        return Result.success(null);
    }

    @PatchMapping("/status/{id}/{status}")
    @Operation(summary = "修改状态", description = "1启用 0禁用")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        sysAiModelService.updateStatus(id, status);
        return Result.success(null);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        sysAiModelService.removeBatchByIds(ids);
        return Result.success(null);
    }
}