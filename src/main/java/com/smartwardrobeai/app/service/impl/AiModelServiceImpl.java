package com.smartwardrobeai.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwardrobeai.admin.model.entity.SysAiModel;
import com.smartwardrobeai.admin.service.SysAIModelService;
import com.smartwardrobeai.app.model.vo.AiModelVO;
import com.smartwardrobeai.app.service.AiModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * App端AI模型服务实现类
 * 提供带缓存的AI模型查询功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelServiceImpl implements AiModelService {

    private final SysAIModelService sysAIModelService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper redisObjectMapper;

    // Redis缓存Key前缀
    private static final String CACHE_PREFIX = "app:ai-model:";
    // 列表缓存Key
    private static final String CACHE_KEY_LIST = CACHE_PREFIX + "list";
    // 详情缓存Key模板
    private static final String CACHE_KEY_DETAIL_TEMPLATE = CACHE_PREFIX + "detail:";
    // 缓存过期时间：24小时
    private static final long CACHE_TTL_HOURS = 24;

    @Override
    public List<AiModelVO> getModelList() {
        // 1. 先查缓存
        try {
            Object cached = redisTemplate.opsForValue().get(CACHE_KEY_LIST);
            if (cached != null) {
                log.debug("从缓存获取AI模型列表");
                // 使用 ObjectMapper 将 LinkedHashMap 转换为 List<AiModelVO>
                List<AiModelVO> result = redisObjectMapper.convertValue(cached, 
                    new TypeReference<List<AiModelVO>>() {});
                return result;
            }
        } catch (Exception e) {
            log.warn("从缓存获取AI模型列表失败，将查询数据库", e);
        }

        // 2. 缓存未命中，查询数据库（仅查询启用状态的模型，按sort排序）
        List<SysAiModel> modelList = sysAIModelService.list(
                new LambdaQueryWrapper<SysAiModel>()
                        .eq(SysAiModel::getStatus, 1)
                        .orderByAsc(SysAiModel::getSort)
        );

        // 3. 转换为VO（过滤敏感信息）
        List<AiModelVO> result = modelList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 4. 写入缓存（即使结果为空也缓存，避免频繁查询空结果）
        try {
            redisTemplate.opsForValue().set(CACHE_KEY_LIST, result, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.debug("AI模型列表已写入缓存，共{}条", result.size());
        } catch (Exception e) {
            log.warn("写入缓存失败", e);
            // 缓存写入失败不影响返回结果
        }

        return result;
    }

    @Override
    public AiModelVO getModelDetail(String modelKey) {
        if (!StringUtils.hasText(modelKey)) {
            log.warn("modelKey为空，返回null");
            return null;
        }

        String cacheKey = CACHE_KEY_DETAIL_TEMPLATE + modelKey;

        // 1. 先查缓存
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("从缓存获取AI模型详情: {}", modelKey);
                // 使用 ObjectMapper 将 LinkedHashMap 转换为 AiModelVO
                return redisObjectMapper.convertValue(cached, AiModelVO.class);
            }
        } catch (Exception e) {
            log.warn("从缓存获取AI模型详情失败: {}, 将查询数据库", modelKey, e);
        }

        // 2. 缓存未命中，调用admin端服务获取详情（该方法已做apiKey脱敏，如果不存在会抛出BusinessException）
        SysAiModel model = sysAIModelService.getModelDetail(modelKey);

        // 3. 转换为VO
        AiModelVO vo = convertToVO(model);

        // 4. 写入缓存
        try {
            redisTemplate.opsForValue().set(cacheKey, vo, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.debug("AI模型详情已写入缓存: {}", modelKey);
        } catch (Exception e) {
            log.warn("写入缓存失败: {}", modelKey, e);
            // 缓存写入失败不影响返回结果
        }

        return vo;
    }

    /**
     * 将SysAiModel实体转换为AiModelVO
     * 确保不包含敏感信息（apiKey等）
     */
    private AiModelVO convertToVO(SysAiModel model) {
        if (model == null) {
            return null;
        }

        // 显式设置字段，确保只复制app端需要的字段，不包含敏感信息
        return AiModelVO.builder()
                .modelKey(model.getModelKey())
                .label(model.getLabel())
                .modelName(model.getModelName())
                .baseUrl(model.getBaseUrl())
                .supportThinking(model.getSupportThinking())
                .maxThinkingBudget(model.getMaxThinkingBudget())
                .defaultEnableThinking(model.getDefaultEnableThinking())
                .defaultThinkingBudget(model.getDefaultThinkingBudget())
                .sort(model.getSort())
                .build();
    }
}

