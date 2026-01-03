package com.smartwardrobeai.app.controller;

import com.smartwardrobeai.common.Result;
import com.smartwardrobeai.common.annotation.NoRepeatSubmit;
import com.smartwardrobeai.app.model.dto.AiExecutionDTO;
import com.smartwardrobeai.app.model.dto.ClothingCreateDTO;
import com.smartwardrobeai.app.model.vo.ClothingAnalysisVO;
import com.smartwardrobeai.app.service.ClothingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("app/api/clothing")
@Tag(name = "衣物管理", description = "核心业务：上传、分析、录入")
@RequiredArgsConstructor
public class ClothingController {

    private final ClothingService clothingService;

    /**
     * Step 1: 智能上传与分析接口
     */
    @NoRepeatSubmit(timeout = 5000)
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传并智能识别", description = "上传图片，后台AI识别，返回预填信息")
    public Result<ClothingAnalysisVO> analyze(
            @Parameter(description = "图片文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "AI配置参数", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @Valid @RequestPart("config") AiExecutionDTO config) {
        ClothingAnalysisVO vo = clothingService.uploadAndAnalyze(file, config);
        return Result.success(vo);
    }

    /**
     * Step 3: 确认新增接口
     */
    @PostMapping("/add")
    @Operation(summary = "确认新增衣物", description = "用户编辑补充信息后，最终保存")
    public Result<Boolean> add(@RequestBody @Valid ClothingCreateDTO dto) {
        boolean success = clothingService.createClothing(dto);
        return Result.success(success);
    }
}