package com.smartwardrobeai.app.service.impl;

import com.smartwardrobeai.admin.service.SysDictDataService;
import com.smartwardrobeai.app.service.DictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * App端字典服务实现类
 * 提供带缓存的字典数据查询功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final SysDictDataService sysDictDataService;
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis缓存Key前缀
    private static final String CACHE_PREFIX = "app:dict:type:";
    // 缓存过期时间：24小时
    private static final long CACHE_TTL_HOURS = 24;

    @Override
    public List<Map<String, String>> getDictByType(String dictType) {
        if (!StringUtils.hasText(dictType)) {
            log.warn("字典类型为空，返回空列表");
            return Collections.emptyList();
        }

        String cacheKey = CACHE_PREFIX + dictType;

        // 1. 先查缓存
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("从缓存获取字典数据: {}", dictType);
                @SuppressWarnings("unchecked")
                List<Map<String, String>> result = (List<Map<String, String>>) cached;
                return result;
            }
        } catch (Exception e) {
            log.warn("从缓存获取字典数据失败: {}, 将查询数据库", dictType, e);
        }

        // 2. 缓存未命中，查数据库
        List<Map<String, String>> result = sysDictDataService.getListByDictType(dictType);

        // 3. 写入缓存（即使结果为空也缓存，避免频繁查询空结果）
        try {
            redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.debug("字典数据已写入缓存: {}", dictType);
        } catch (Exception e) {
            log.warn("写入缓存失败: {}", dictType, e);
            // 缓存写入失败不影响返回结果
        }

        return result;
    }

    @Override
    public Map<String, List<Map<String, String>>> getDictBatch(List<String> dictTypes) {
        if (dictTypes == null || dictTypes.isEmpty()) {
            return Collections.emptyMap();
        }

        // 去重并过滤空值
        List<String> uniqueTypes = dictTypes.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .sorted() // 排序保证缓存key的一致性
                .collect(Collectors.toList());

        if (uniqueTypes.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, List<Map<String, String>>> result = new LinkedHashMap<>();

        // 先尝试从缓存获取
        List<String> missTypes = new ArrayList<>();
        for (String dictType : uniqueTypes) {
            String cacheKey = CACHE_PREFIX + dictType;
            try {
                Object cached = redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    log.debug("从缓存获取字典数据: {}", dictType);
                    @SuppressWarnings("unchecked")
                    List<Map<String, String>> cachedData = (List<Map<String, String>>) cached;
                    result.put(dictType, cachedData);
                } else {
                    missTypes.add(dictType);
                }
            } catch (Exception e) {
                log.warn("从缓存获取字典数据失败: {}, 将查询数据库", dictType, e);
                missTypes.add(dictType);
            }
        }

        // 批量查询未命中的字典数据
        if (!missTypes.isEmpty()) {
            for (String dictType : missTypes) {
                List<Map<String, String>> data = sysDictDataService.getListByDictType(dictType);
                result.put(dictType, data);

                // 写入缓存
                String cacheKey = CACHE_PREFIX + dictType;
                try {
                    redisTemplate.opsForValue().set(cacheKey, data, CACHE_TTL_HOURS, TimeUnit.HOURS);
                    log.debug("字典数据已写入缓存: {}", dictType);
                } catch (Exception e) {
                    log.warn("写入缓存失败: {}", dictType, e);
                }
            }
        }

        return result;
    }
}

