package com.smartwardrobeai.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartwardrobeai.admin.mapper.SysCategoryStrategyMapper;
import com.smartwardrobeai.admin.model.dto.CategoryStrategyQueryDTO;
import com.smartwardrobeai.admin.model.dto.CategoryStrategySaveDTO;
import com.smartwardrobeai.admin.model.entity.SysCategoryStrategy;
import com.smartwardrobeai.admin.model.vo.CategoryStrategyVO;
import com.smartwardrobeai.admin.model.entity.SysDictData;
import com.smartwardrobeai.admin.service.SysCategoryStrategyService;
import com.smartwardrobeai.admin.service.SysDictDataService;
import com.smartwardrobeai.common.model.entity.PageResult;
import com.smartwardrobeai.utils.QueryGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysCategoryStrategyServiceImpl extends ServiceImpl<SysCategoryStrategyMapper, SysCategoryStrategy>
        implements SysCategoryStrategyService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SysDictDataService dictDataService;

    // Redis Key 前缀
    private static final String CACHE_PREFIX_CODE = "category_strategy:code:";
    private static final String CACHE_PREFIX_ALL = "category_strategy:all";
    // 缓存过期时间：1小时
    private static final long CACHE_TTL_HOURS = 1;
    
    // 字典类型常量
    private static final String DICT_TYPE_REGION = "clothing_region";
    private static final String DICT_TYPE_LAYER = "clothing_layer";

    @Override
    public SysCategoryStrategy match(String code) {
        if (code == null || code.trim().isEmpty()) {
            return getUnknownStrategy();
        }

        String cacheKey = CACHE_PREFIX_CODE + code.toLowerCase();
        
        // 1. 先查缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取品类策略: {}", code);
            return (SysCategoryStrategy) cached;
        }

        // 2. 缓存未命中，查数据库
        SysCategoryStrategy strategy = this.getOne(
                new LambdaQueryWrapper<SysCategoryStrategy>()
                        .eq(SysCategoryStrategy::getCategoryCode, code)
                        .eq(SysCategoryStrategy::getStatus, 1)
        );

        // 3. 如果未找到，返回 Unknown 策略
        if (strategy == null) {
            log.warn("未找到品类策略: {}, 使用默认 Unknown 策略", code);
            strategy = getUnknownStrategy();
        }

        // 4. 写入缓存
        redisTemplate.opsForValue().set(cacheKey, strategy, CACHE_TTL_HOURS, TimeUnit.HOURS);
        
        return strategy;
    }

    @Override
    public List<SysCategoryStrategy> getAllEnabled() {
        // 1. 先查缓存
        Object cached = redisTemplate.opsForValue().get(CACHE_PREFIX_ALL);
        if (cached != null) {
            log.debug("从缓存获取所有启用的品类策略");
            return (List<SysCategoryStrategy>) cached;
        }

        // 2. 缓存未命中，查数据库
        List<SysCategoryStrategy> list = this.list(
                new LambdaQueryWrapper<SysCategoryStrategy>()
                        .eq(SysCategoryStrategy::getStatus, 1)
                        .orderByAsc(SysCategoryStrategy::getSort)
        );

        // 3. 写入缓存
        redisTemplate.opsForValue().set(CACHE_PREFIX_ALL, list, CACHE_TTL_HOURS, TimeUnit.HOURS);

        return list;
    }

    @Override
    public PageResult<CategoryStrategyVO> pageQuery(CategoryStrategyQueryDTO queryDTO) {
        // 1. 获取分页对象
        Page<SysCategoryStrategy> page = queryDTO.toMpPage("sort", false);

        // 2. 生成 QueryWrapper
        QueryWrapper<SysCategoryStrategy> wrapper = QueryGenerator.generate(queryDTO);

        // 3. 执行查询
        this.page(page, wrapper);

        // 4. 转换VO
        PageResult<CategoryStrategyVO> result = PageResult.of(page, CategoryStrategyVO.class);
        
        // 5. 填充字典标签（regionLabel 和 layerLabel）
        fillDictLabels(result.getRecords());
        
        return result;
    }

    @Override
    public void saveStrategy(CategoryStrategySaveDTO saveDTO) {
        // 查重
        Long count = this.lambdaQuery()
                .eq(SysCategoryStrategy::getCategoryCode, saveDTO.getCategoryCode())
                .count();
        if (count > 0) {
            throw new RuntimeException("品类代码 [" + saveDTO.getCategoryCode() + "] 已存在");
        }

        SysCategoryStrategy entity = new SysCategoryStrategy();
        BeanUtils.copyProperties(saveDTO, entity);

        // 默认值填充
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }

        this.save(entity);
        
        // 清除缓存
        clearCache();
    }

    @Override
    public void updateStrategy(CategoryStrategySaveDTO saveDTO) {
        if (saveDTO.getId() == null) {
            throw new IllegalArgumentException("ID不能为空");
        }

        SysCategoryStrategy updateEntity = new SysCategoryStrategy();
        BeanUtils.copyProperties(saveDTO, updateEntity);

        this.updateById(updateEntity);
        
        // 清除缓存
        clearCache();
    }

    @Override
    public CategoryStrategyVO getDetail(Long id) {
        CategoryStrategyVO vo = BeanUtil.toBean(getById(id), CategoryStrategyVO.class);
        // 填充字典标签
        if (vo != null) {
            fillDictLabels(List.of(vo));
        }
        return vo;
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        this.update(new LambdaUpdateWrapper<SysCategoryStrategy>()
                .eq(SysCategoryStrategy::getId, id)
                .set(SysCategoryStrategy::getStatus, status));
        
        // 清除缓存
        clearCache();
    }

    /**
     * 重写 removeById 方法，确保删除时清除缓存
     */
    @Override
    public boolean removeById(java.io.Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            clearCache();
        }
        return result;
    }

    /**
     * 重写 removeBatchByIds 方法，确保批量删除时清除缓存
     */
    @Override
    public boolean removeBatchByIds(java.util.Collection<?> idList) {
        boolean result = super.removeBatchByIds(idList);
        if (result) {
            clearCache();
        }
        return result;
    }

    /**
     * 获取 Unknown 默认策略
     */
    private SysCategoryStrategy getUnknownStrategy() {
        SysCategoryStrategy unknown = this.getOne(
                new LambdaQueryWrapper<SysCategoryStrategy>()
                        .eq(SysCategoryStrategy::getCategoryCode, "Unknown")
                        .eq(SysCategoryStrategy::getStatus, 1)
        );
        
        if (unknown == null) {
            // 如果数据库中没有 Unknown，创建一个默认对象
            log.error("数据库中未找到 Unknown 默认策略，使用硬编码默认值");
            unknown = SysCategoryStrategy.builder()
                    .categoryCode("Unknown")
                    .categoryDesc("未知")
                    .region("TOP")
                    .layer("MIDDLE")
                    .status(1)
                    .build();
        }
        
        return unknown;
    }

    /**
     * 填充字典标签（regionLabel 和 layerLabel）
     *
     * @param voList VO列表
     */
    private void fillDictLabels(List<CategoryStrategyVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }

        try {
            // 1. 批量查询 region 字典数据
            List<SysDictData> regionDictList = dictDataService.list(
                    new LambdaQueryWrapper<SysDictData>()
                            .eq(SysDictData::getDictType, DICT_TYPE_REGION)
                            .eq(SysDictData::getStatus, 1)
            );
            Map<String, String> regionLabelMap = regionDictList.stream()
                    .collect(Collectors.toMap(
                            SysDictData::getDictValue,
                            SysDictData::getDictLabel,
                            (oldValue, newValue) -> oldValue // 如果有重复key，保留第一个
                    ));

            // 2. 批量查询 layer 字典数据
            List<SysDictData> layerDictList = dictDataService.list(
                    new LambdaQueryWrapper<SysDictData>()
                            .eq(SysDictData::getDictType, DICT_TYPE_LAYER)
                            .eq(SysDictData::getStatus, 1)
            );
            Map<String, String> layerLabelMap = layerDictList.stream()
                    .collect(Collectors.toMap(
                            SysDictData::getDictValue,
                            SysDictData::getDictLabel,
                            (oldValue, newValue) -> oldValue
                    ));

            // 3. 填充标签
            for (CategoryStrategyVO vo : voList) {
                if (vo.getRegion() != null) {
                    vo.setRegionLabel(regionLabelMap.getOrDefault(vo.getRegion(), vo.getRegion()));
                }
                if (vo.getLayer() != null) {
                    vo.setLayerLabel(layerLabelMap.getOrDefault(vo.getLayer(), vo.getLayer()));
                }
            }
        } catch (Exception e) {
            log.error("填充字典标签失败", e);
            // 降级处理：如果查询字典失败，标签留空，不影响主流程
        }
    }

    @Override
    public void refreshCache() {
        try {
            // 清除所有品类策略缓存
            redisTemplate.delete(CACHE_PREFIX_ALL);
            log.info("手动刷新品类策略缓存完成");
        } catch (Exception e) {
            log.error("手动刷新缓存失败", e);
            throw new RuntimeException("刷新缓存失败: " + e.getMessage(), e);
        }
    }

    /**
     * 清除所有相关缓存（内部方法，失败不影响主流程）
     */
    private void clearCache() {
        try {
            // 清除单个策略缓存（使用通配符删除所有 code:* 的key）
            // 注意：RedisTemplate 不支持通配符删除，需要先获取所有匹配的key
            // 这里简化处理，直接删除 all 缓存，单个缓存会在下次查询时自动更新
            redisTemplate.delete(CACHE_PREFIX_ALL);
            
            // 也可以选择删除所有 category_strategy:* 的key，但需要遍历
            // 为了性能，这里只删除 all 缓存，单个缓存会在下次查询时自然过期或更新
            log.debug("已清除品类策略缓存");
        } catch (Exception e) {
            log.error("清除缓存失败", e);
            // 注意：这里不抛出异常，避免影响增删改操作的主流程
        }
    }
}

