package com.smartwardrobeai.app.controller;

import com.smartwardrobeai.app.model.vo.AiModelVO;
import com.smartwardrobeai.app.service.AiModelService;
import com.smartwardrobeai.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * App端AI模型查询控制器
 * 提供AI模型列表和详情查询接口
 */
@RestController
@RequestMapping("api/app/ai-models")
@Tag(name = "AI模型配置", description = "App端AI模型查询接口，提供模型列表和详情查询")
@RequiredArgsConstructor
public class AiModelController {

    private final AiModelService aiModelService;

    /**
     * 获取所有启用状态的AI模型列表（完整信息）
     * 包含思考模式配置等信息，按sort排序
     */
    @GetMapping("/list")
    @Operation(summary = "获取AI模型列表", description = "返回所有启用状态的AI模型完整信息列表，包含思考模式配置等，按排序值升序排列")
    public Result<List<AiModelVO>> getModelList() {
        List<AiModelVO> modelList = aiModelService.getModelList();
        return Result.success(modelList);
    }

    /**
     * 根据modelKey获取AI模型详情
     * 包含思考模式配置等信息
     */
    @GetMapping("/detail")
    @Operation(summary = "获取AI模型详情", description = "根据modelKey获取指定AI模型的详细信息，包含思考模式配置等")
    public Result<AiModelVO> getModelDetail(
            @Parameter(description = "模型唯一标识Key", required = true, example = "qwen-plus")
            @RequestParam @NotBlank(message = "modelKey不能为空") String modelKey) {
        AiModelVO modelDetail = aiModelService.getModelDetail(modelKey);
        return Result.success(modelDetail);
    }
}