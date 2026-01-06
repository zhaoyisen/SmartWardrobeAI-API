package com.smartwardrobeai.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 仪表盘统计数据VO
 */
@Data
@Schema(description = "仪表盘统计数据")
public class DashboardVO {

    @Schema(description = "基础统计")
    private BasicStatistics basicStatistics;

    @Schema(description = "数据趋势（最近7天）")
    private List<TrendDataVO> trendData7Days;

    @Schema(description = "数据趋势（最近30天）")
    private List<TrendDataVO> trendData30Days;

    @Schema(description = "衣物分类统计")
    private CategoryStatistics categoryStatistics;

    @Schema(description = "最近操作记录")
    private RecentActivities recentActivities;

    @Schema(description = "存储信息")
    private StorageStatisticsVO storageInfo;

    @Schema(description = "用户活跃度")
    private UserActivity userActivity;

    /**
     * 基础统计
     */
    @Data
    @Schema(description = "基础统计")
    public static class BasicStatistics {
        @Schema(description = "字典类型数")
        private Long dictTypeCount;

        @Schema(description = "字典数据数")
        private Long dictDataCount;

        @Schema(description = "AI模型数")
        private Long aiModelCount;

        @Schema(description = "用户总数（管理端+App端）")
        private Long totalUserCount;

        @Schema(description = "管理端用户数")
        private Long sysUserCount;

        @Schema(description = "App端用户数")
        private Long appUserCount;

        @Schema(description = "衣物总数")
        private Long clothingCount;

        @Schema(description = "文件总数")
        private Long fileCount;
    }

    /**
     * 分类统计
     */
    @Data
    @Schema(description = "分类统计")
    public static class CategoryStatistics {
        @Schema(description = "按部位统计")
        private List<CategoryStatsVO> regionStats;

        @Schema(description = "按品类统计")
        private List<CategoryStatsVO> categoryStats;

        @Schema(description = "按季节统计")
        private List<CategoryStatsVO> seasonStats;
    }

    /**
     * 最近操作记录
     */
    @Data
    @Schema(description = "最近操作记录")
    public static class RecentActivities {
        @Schema(description = "最近新增的衣物（最多10条）")
        private List<RecentClothingVO> recentClothing;

        @Schema(description = "最近注册的用户（最多10条）")
        private List<RecentUserVO> recentUsers;
    }

    /**
     * 用户活跃度
     */
    @Data
    @Schema(description = "用户活跃度")
    public static class UserActivity {
        @Schema(description = "日活（当天有操作的用户数）")
        private Long dailyActiveUsers;

        @Schema(description = "月活（最近30天有操作的用户数）")
        private Long monthlyActiveUsers;
    }
}

