package com.smartwardrobeai.app.controller;

import com.smartwardrobeai.common.Result;
import com.smartwardrobeai.common.annotation.NoRepeatSubmit;
import com.smartwardrobeai.app.model.dto.AiExecutionDTO;
import com.smartwardrobeai.app.model.dto.ClothingCreateDTO;
import com.smartwardrobeai.app.model.dto.ClothingQueryDTO;
import com.smartwardrobeai.app.model.vo.ClothingAnalysisVO;
import com.smartwardrobeai.app.model.vo.ClothingFilterOptionsVO;
import com.smartwardrobeai.app.model.vo.ClothingVO;
import com.smartwardrobeai.app.service.ClothingService;
import com.smartwardrobeai.common.model.entity.PageResult;
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
@RequestMapping("api/app/clothing")
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
     * 保存衣物（新增或编辑）
     */
    @PostMapping("/save")
    @Operation(summary = "保存衣物", description = "新增或编辑衣物。当DTO中id为空时表示新增，不为空时表示编辑")
    public Result<Boolean> save(@RequestBody @Valid ClothingCreateDTO dto) {
        boolean success = clothingService.saveClothing(dto);
        return Result.success(success);
    }

    /**
     * 查询衣橱列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询衣橱列表", description = "根据部位筛选当前用户的衣物，支持分页。region为空表示查询全部")
    public Result<PageResult<ClothingVO>> list(ClothingQueryDTO queryDTO) {
        PageResult<ClothingVO> result = clothingService.queryClothingList(queryDTO);
        return Result.success(result);
    }

    /**
     * 删除衣物（逻辑删除）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除衣物", description = "逻辑删除衣物，将del_flag设置为1")
    public Result<Boolean> delete(@Parameter(description = "衣物ID", required = true) @PathVariable Long id) {
        boolean success = clothingService.deleteClothing(id);
        return Result.success(success);
    }

    /**
     * 获取用户衣物的筛选选项
     */
    @GetMapping("/filter-options")
    @Operation(summary = "获取筛选选项", description = "查询当前用户衣物已有的筛选条件选项，用于前端展示查询条件")
    public Result<ClothingFilterOptionsVO> getFilterOptions() {
        ClothingFilterOptionsVO options = clothingService.getFilterOptions();
        return Result.success(options);
    }
}