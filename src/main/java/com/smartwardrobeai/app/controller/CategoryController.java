package com.smartwardrobeai.app.controller;

import com.smartwardrobeai.app.model.vo.CategoryVO;
import com.smartwardrobeai.app.service.CategoryService;
import com.smartwardrobeai.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * App端品类查询控制器
 * 提供品类列表查询接口
 */
@RestController
@RequestMapping("api/app/categories")
@Tag(name = "品类数据", description = "App端品类查询接口，提供品类列表查询")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取所有启用的品类列表
     * 按sort排序
     */
    @GetMapping("/list")
    @Operation(summary = "获取品类列表", description = "返回所有启用状态的品类列表，包含品类代码、中文描述、部位、层级等字段，按排序值升序排列")
    public Result<List<CategoryVO>> getCategoryList() {
        List<CategoryVO> categoryList = categoryService.getCategoryList();
        return Result.success(categoryList);
    }
}

