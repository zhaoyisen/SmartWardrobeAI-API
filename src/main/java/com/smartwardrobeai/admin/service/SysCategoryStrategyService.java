package com.smartwardrobeai.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartwardrobeai.admin.model.dto.CategoryStrategyQueryDTO;
import com.smartwardrobeai.admin.model.dto.CategoryStrategySaveDTO;
import com.smartwardrobeai.admin.model.entity.SysCategoryStrategy;
import com.smartwardrobeai.admin.model.vo.CategoryStrategyVO;
import com.smartwardrobeai.common.model.entity.PageResult;

import java.util.List;

public interface SysCategoryStrategyService extends IService<SysCategoryStrategy> {

    /**
     * 根据品类代码查找策略（带缓存）
     *
     * @param code 品类代码（如 "T-shirt"）
     * @return 品类策略实体，如果未找到则返回 Unknown 策略
     */
    SysCategoryStrategy match(String code);

    /**
     * 获取所有启用的策略（带缓存）
     *
     * @return 启用的策略列表
     */
    List<SysCategoryStrategy> getAllEnabled();

    /**
     * 分页查询
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<CategoryStrategyVO> pageQuery(CategoryStrategyQueryDTO queryDTO);

    /**
     * 保存策略
     *
     * @param saveDTO 保存参数
     */
    void saveStrategy(CategoryStrategySaveDTO saveDTO);

    /**
     * 更新策略
     *
     * @param saveDTO 更新参数
     */
    void updateStrategy(CategoryStrategySaveDTO saveDTO);

    /**
     * 获取详情
     *
     * @param id 主键ID
     * @return 详情VO
     */
    CategoryStrategyVO getDetail(Long id);

    /**
     * 更新状态
     *
     * @param id     主键ID
     * @param status 状态（1启用 0禁用）
     */
    void updateStatus(Long id, Integer status);

    /**
     * 手动刷新缓存
     * 清除所有品类策略相关的缓存，下次查询时会重新从数据库加载
     */
    void refreshCache();
}

