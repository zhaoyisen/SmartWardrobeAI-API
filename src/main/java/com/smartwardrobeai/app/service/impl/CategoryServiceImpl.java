package com.smartwardrobeai.app.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwardrobeai.admin.model.entity.SysCategoryStrategy;
import com.smartwardrobeai.admin.service.SysCategoryStrategyService;
import com.smartwardrobeai.app.model.vo.CategoryVO;
import com.smartwardrobeai.app.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * App端品类服务实现类
 * 提供带缓存的品类查询功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final SysCategoryStrategyService sysCategoryStrategyService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper redisObjectMapper;

    // Redis缓存Key
    private static final String CACHE_KEY_LIST = "app:category:list";
    // 缓存过期时间：24小时
    private static final long CACHE_TTL_HOURS = 24;

    @Override
    public List<CategoryVO> getCategoryList() {
        // 1. 先查缓存
        try {
            Object cached = redisTemplate.opsForValue().get(CACHE_KEY_LIST);
            if (cached != null) {
                log.debug("从缓存获取品类列表");
                // 使用 ObjectMapper 将 LinkedHashMap 转换为 List<CategoryVO>
                List<CategoryVO> result = redisObjectMapper.convertValue(cached,
                    new TypeReference<List<CategoryVO>>() {});
                if (result != null && result.size() > 0) {
                    return result;
                }
            }
        } catch (Exception e) {
            log.warn("从缓存获取品类列表失败，将查询数据库", e);
        }

        // 2. 缓存未命中，调用admin端服务获取数据（该方法已有缓存机制）
        List<SysCategoryStrategy> strategyList = sysCategoryStrategyService.getAllEnabled();

        // 3. 转换为VO
        List<CategoryVO> result = strategyList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 4. 写入缓存（即使结果为空也缓存，避免频繁查询空结果）
        try {
            redisTemplate.opsForValue().set(CACHE_KEY_LIST, result, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.debug("品类列表已写入缓存，共{}条", result.size());
        } catch (Exception e) {
            log.warn("写入缓存失败", e);
            // 缓存写入失败不影响返回结果
        }

        return result;
    }

    /**
     * 将SysCategoryStrategy实体转换为CategoryVO
     * 只包含app端需要的核心字段
     */
    private CategoryVO convertToVO(SysCategoryStrategy strategy) {
        if (strategy == null) {
            return null;
        }

        return CategoryVO.builder()
                .categoryCode(strategy.getCategoryCode())
                .categoryDesc(strategy.getCategoryDesc())
                .region(strategy.getRegion())
                .layer(strategy.getLayer())
                .sort(strategy.getSort())
                .build();
    }
}

