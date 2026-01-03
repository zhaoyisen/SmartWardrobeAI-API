package com.smartwardrobeai.app.controller;

import com.smartwardrobeai.common.Result;
import com.smartwardrobeai.app.model.entity.SysAiModel;
import com.smartwardrobeai.app.service.SysAIModelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("app/api/ai-models")
@Tag(name = "AI 模型配置", description = "前端获取模型列表和详情")
@RequiredArgsConstructor
public class AiModelController {
    private final SysAIModelService sysAIModelService;

    @GetMapping("/list")
    public Result<List<Map<String, String>>> getModelList() {
        // 逻辑全在 Service，这里只管调
        return Result.success(sysAIModelService.getDropdownList());
    }

    @GetMapping("/detail")
    public Result<SysAiModel> getModelDetail(@RequestParam String modelKey) {
        // 逻辑全在 Service，这里只管调
        return Result.success(sysAIModelService.getModelDetail(modelKey));
    }
}