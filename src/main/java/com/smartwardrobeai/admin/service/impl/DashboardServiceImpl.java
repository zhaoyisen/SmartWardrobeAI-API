package com.smartwardrobeai.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartwardrobeai.admin.mapper.SysAiModelMapper;
import com.smartwardrobeai.admin.mapper.SysDictDataMapper;
import com.smartwardrobeai.admin.mapper.SysDictTypeMapper;
import com.smartwardrobeai.admin.mapper.SysUserMapper;
import com.smartwardrobeai.admin.model.entity.SysAiModel;
import com.smartwardrobeai.admin.model.entity.SysDictData;
import com.smartwardrobeai.admin.model.entity.SysDictType;
import com.smartwardrobeai.admin.model.entity.SysUser;
import com.smartwardrobeai.admin.model.vo.*;
import com.smartwardrobeai.admin.service.DashboardService;
import com.smartwardrobeai.admin.service.StorageAdminService;
import com.smartwardrobeai.app.mapper.ClothingMapper;
import com.smartwardrobeai.app.mapper.SysFileMapper;
import com.smartwardrobeai.app.mapper.UserMapper;
import com.smartwardrobeai.app.model.entity.Clothing;
import com.smartwardrobeai.app.model.entity.SysFile;
import com.smartwardrobeai.app.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;
    private final SysAiModelMapper aiModelMapper;
    private final SysUserMapper sysUserMapper;
    private final UserMapper userMapper;
    private final ClothingMapper clothingMapper;
    private final SysFileMapper sysFileMapper;
    private final StorageAdminService storageAdminService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public DashboardVO getStatistics() {
        DashboardVO dashboardVO = new DashboardVO();

        // 基础统计
        dashboardVO.setBasicStatistics(getBasicStatistics());

        // 数据趋势
        dashboardVO.setTrendData7Days(getTrendData(7));
        dashboardVO.setTrendData30Days(getTrendData(30));

        // 分类统计
        dashboardVO.setCategoryStatistics(getCategoryStatistics());

        // 最近操作记录
        dashboardVO.setRecentActivities(getRecentActivities());

        // 存储信息
        dashboardVO.setStorageInfo(storageAdminService.getStatistics());

        // 用户活跃度
        dashboardVO.setUserActivity(getUserActivity());

        return dashboardVO;
    }

    /**
     * 获取基础统计
     */
    private DashboardVO.BasicStatistics getBasicStatistics() {
        DashboardVO.BasicStatistics stats = new DashboardVO.BasicStatistics();

        // 字典类型数
        stats.setDictTypeCount(dictTypeMapper.selectCount(null));

        // 字典数据数
        stats.setDictDataCount(dictDataMapper.selectCount(null));

        // AI模型数
        stats.setAiModelCount(aiModelMapper.selectCount(null));

        // 管理端用户数
        stats.setSysUserCount(sysUserMapper.selectCount(null));

        // App端用户数
        stats.setAppUserCount(userMapper.selectCount(null));

        // 用户总数
        stats.setTotalUserCount(stats.getSysUserCount() + stats.getAppUserCount());

        // 衣物总数（排除逻辑删除）
        LambdaQueryWrapper<Clothing> clothingWrapper = new LambdaQueryWrapper<>();
        clothingWrapper.eq(Clothing::getDelFlag, 0);
        stats.setClothingCount(clothingMapper.selectCount(clothingWrapper));

        // 文件总数
        stats.setFileCount(sysFileMapper.selectCount(null));

        return stats;
    }

    /**
     * 获取趋势数据
     */
    private List<TrendDataVO> getTrendData(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // 初始化日期列表
        Map<LocalDate, TrendDataVO> trendMap = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            TrendDataVO vo = TrendDataVO.builder()
                    .date(date)
                    .dateStr(date.format(DATE_FORMATTER))
                    .clothingCount(0L)
                    .userCount(0L)
                    .fileCount(0L)
                    .build();
            trendMap.put(date, vo);
        }

        // 查询衣物数据
        LambdaQueryWrapper<Clothing> clothingWrapper = new LambdaQueryWrapper<>();
        clothingWrapper.eq(Clothing::getDelFlag, 0)
                .ge(Clothing::getCreateTime, startDate.atStartOfDay())
                .le(Clothing::getCreateTime, endDate.atTime(23, 59, 59));
        List<Clothing> clothingList = clothingMapper.selectList(clothingWrapper);
        for (Clothing clothing : clothingList) {
            LocalDate date = clothing.getCreateTime().toLocalDate();
            if (trendMap.containsKey(date)) {
                trendMap.get(date).setClothingCount(
                        trendMap.get(date).getClothingCount() + 1
                );
            }
        }

        // 查询用户数据（App端）
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.ge(User::getCreateTime, startDate.atStartOfDay())
                .le(User::getCreateTime, endDate.atTime(23, 59, 59));
        List<User> userList = userMapper.selectList(userWrapper);
        for (User user : userList) {
            LocalDate date = user.getCreateTime().toLocalDate();
            if (trendMap.containsKey(date)) {
                trendMap.get(date).setUserCount(
                        trendMap.get(date).getUserCount() + 1
                );
            }
        }

        // 查询文件数据
        LambdaQueryWrapper<SysFile> fileWrapper = new LambdaQueryWrapper<>();
        fileWrapper.ge(SysFile::getCreateTime, startDate.atStartOfDay())
                .le(SysFile::getCreateTime, endDate.atTime(23, 59, 59));
        List<SysFile> fileList = sysFileMapper.selectList(fileWrapper);
        for (SysFile file : fileList) {
            LocalDate date = file.getCreateTime().toLocalDate();
            if (trendMap.containsKey(date)) {
                trendMap.get(date).setFileCount(
                        trendMap.get(date).getFileCount() + 1
                );
            }
        }

        return new ArrayList<>(trendMap.values());
    }

    /**
     * 获取分类统计
     */
    private DashboardVO.CategoryStatistics getCategoryStatistics() {
        DashboardVO.CategoryStatistics categoryStats = new DashboardVO.CategoryStatistics();

        // 查询所有衣物（排除逻辑删除）
        LambdaQueryWrapper<Clothing> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Clothing::getDelFlag, 0);
        List<Clothing> clothingList = clothingMapper.selectList(wrapper);

        long totalCount = clothingList.size();

        // 按部位统计
        Map<String, Long> regionMap = clothingList.stream()
                .filter(c -> c.getRegion() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getRegion().getCode(),
                        Collectors.counting()
                ));
        categoryStats.setRegionStats(convertToCategoryStatsVO(regionMap, totalCount));

        // 按品类统计
        Map<String, Long> categoryMap = clothingList.stream()
                .filter(c -> c.getCategory() != null)
                .collect(Collectors.groupingBy(
                        Clothing::getCategory,
                        Collectors.counting()
                ));
        categoryStats.setCategoryStats(convertToCategoryStatsVO(categoryMap, totalCount));

        // 按季节统计
        Map<String, Long> seasonMap = clothingList.stream()
                .filter(c -> c.getSeason() != null)
                .collect(Collectors.groupingBy(
                        Clothing::getSeason,
                        Collectors.counting()
                ));
        categoryStats.setSeasonStats(convertToCategoryStatsVO(seasonMap, totalCount));

        return categoryStats;
    }

    /**
     * 转换为CategoryStatsVO列表
     */
    private List<CategoryStatsVO> convertToCategoryStatsVO(Map<String, Long> map, long totalCount) {
        List<CategoryStatsVO> list = new ArrayList<>();
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            double percentage = totalCount > 0 ? (entry.getValue() * 100.0 / totalCount) : 0.0;
            CategoryStatsVO vo = CategoryStatsVO.builder()
                    .name(entry.getKey())
                    .value(entry.getKey())
                    .count(entry.getValue())
                    .percentage(Math.round(percentage * 100.0) / 100.0) // 保留两位小数
                    .build();
            list.add(vo);
        }
        // 按数量降序排序
        list.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        return list;
    }

    /**
     * 获取最近操作记录
     */
    private DashboardVO.RecentActivities getRecentActivities() {
        DashboardVO.RecentActivities activities = new DashboardVO.RecentActivities();

        // 查询最近新增的10条衣物
        LambdaQueryWrapper<Clothing> clothingWrapper = new LambdaQueryWrapper<>();
        clothingWrapper.eq(Clothing::getDelFlag, 0)
                .orderByDesc(Clothing::getCreateTime)
                .last("LIMIT 10");
        List<Clothing> recentClothingList = clothingMapper.selectList(clothingWrapper);

        List<RecentClothingVO> recentClothingVOList = new ArrayList<>();
        for (Clothing clothing : recentClothingList) {
            RecentClothingVO vo = RecentClothingVO.builder()
                    .id(clothing.getId())
                    .name(clothing.getName())
                    .region(clothing.getRegion() != null ? clothing.getRegion().getCode() : null)
                    .category(clothing.getCategory())
                    .userId(clothing.getUserId())
                    .createTime(clothing.getCreateTime())
                    .build();

            // 查询用户名
            if (clothing.getUserId() != null) {
                User user = userMapper.selectById(clothing.getUserId());
                if (user != null) {
                    vo.setUsername(user.getUsername());
                }
            }

            recentClothingVOList.add(vo);
        }
        activities.setRecentClothing(recentClothingVOList);

        // 查询最近注册的10个用户（App端）
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.orderByDesc(User::getCreateTime)
                .last("LIMIT 10");
        List<User> recentUserList = userMapper.selectList(userWrapper);

        List<RecentUserVO> recentUserVOList = recentUserList.stream()
                .map(user -> RecentUserVO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .createTime(user.getCreateTime())
                        .build())
                .collect(Collectors.toList());
        activities.setRecentUsers(recentUserVOList);

        return activities;
    }

    /**
     * 获取用户活跃度
     */
    private DashboardVO.UserActivity getUserActivity() {
        DashboardVO.UserActivity activity = new DashboardVO.UserActivity();

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.minusDays(29);

        // 日活：当天有操作的用户数（通过衣物表的create_time或update_time判断）
        LambdaQueryWrapper<Clothing> dailyWrapper = new LambdaQueryWrapper<>();
        dailyWrapper.eq(Clothing::getDelFlag, 0)
                .ge(Clothing::getCreateTime, today.atStartOfDay())
                .le(Clothing::getCreateTime, today.atTime(23, 59, 59));
        List<Clothing> dailyClothingList = clothingMapper.selectList(dailyWrapper);
        Set<Long> dailyActiveUserIds = dailyClothingList.stream()
                .map(Clothing::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        activity.setDailyActiveUsers((long) dailyActiveUserIds.size());

        // 月活：最近30天有操作的用户数
        LambdaQueryWrapper<Clothing> monthlyWrapper = new LambdaQueryWrapper<>();
        monthlyWrapper.eq(Clothing::getDelFlag, 0)
                .ge(Clothing::getCreateTime, monthStart.atStartOfDay())
                .le(Clothing::getCreateTime, today.atTime(23, 59, 59));
        List<Clothing> monthlyClothingList = clothingMapper.selectList(monthlyWrapper);
        Set<Long> monthlyActiveUserIds = monthlyClothingList.stream()
                .map(Clothing::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        activity.setMonthlyActiveUsers((long) monthlyActiveUserIds.size());

        return activity;
    }
}

