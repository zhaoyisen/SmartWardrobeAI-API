package com.smartwardrobeai.admin.controller;

import com.smartwardrobeai.admin.model.vo.DashboardVO;
import com.smartwardrobeai.admin.service.DashboardService;
import com.smartwardrobeai.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘控制器
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "【后台】仪表盘管理")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/statistics")
    @Operation(summary = "获取仪表盘统计数据", description = "返回首页所需的所有统计数据，包括基础统计、数据趋势、分类统计、最近操作、存储信息和用户活跃度等")
    public Result<DashboardVO> getStatistics() {
        DashboardVO statistics = dashboardService.getStatistics();
        return Result.success(statistics);
    }
}

