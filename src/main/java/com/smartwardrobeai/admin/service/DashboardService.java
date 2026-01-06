package com.smartwardrobeai.admin.service;

import com.smartwardrobeai.admin.model.vo.DashboardVO;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {

    /**
     * 获取仪表盘统计数据
     *
     * @return 仪表盘统计数据
     */
    DashboardVO getStatistics();
}

