package com.smartwardrobeai.app.controller;

import com.smartwardrobeai.app.service.DictService;
import com.smartwardrobeai.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * App端字典数据控制器
 * 提供字典数据查询接口，包含单个和批量查询
 */
@RestController
@RequestMapping("api/app/dict")
@Tag(name = "字典数据", description = "App端字典数据查询接口")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    /**
     * 根据字典类型编码获取字典数据列表
     *
     * @param dictType 字典类型编码（如：clothing_color）
     * @return 字典数据列表，包含 value、label、promptText 字段
     */
    @GetMapping("/{dictType}")
    @Operation(summary = "获取单个字典数据", description = "根据字典类型编码获取启用的字典数据列表，包含value、label、promptText字段")
    public Result<List<Map<String, String>>> getDictByType(
            @Parameter(description = "字典类型编码，如：clothing_color", required = true)
            @PathVariable String dictType) {
        List<Map<String, String>> result = dictService.getDictByType(dictType);
        return Result.success(result);
    }

    /**
     * 批量获取多个字典类型的数据
     *
     * @param types 字典类型编码列表，多个用逗号分隔（如：clothing_color,gender）
     * @return Map结构，key为字典类型编码，value为字典数据列表
     */
    @GetMapping("/batch")
    @Operation(summary = "批量获取字典数据", description = "一次获取多个字典类型的数据，多个类型用逗号分隔，返回Map结构")
    public Result<Map<String, List<Map<String, String>>>> getDictBatch(
            @Parameter(description = "字典类型编码列表，多个用逗号分隔，如：clothing_color,gender", required = true)
            @RequestParam("types") String types) {
        if (!StringUtils.hasText(types)) {
            return Result.success(Collections.emptyMap());
        }

        // 解析逗号分隔的字符串
        List<String> dictTypes = Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());

        if (dictTypes.isEmpty()) {
            return Result.success(Collections.emptyMap());
        }

        Map<String, List<Map<String, String>>> result = dictService.getDictBatch(dictTypes);
        return Result.success(result);
    }
}

