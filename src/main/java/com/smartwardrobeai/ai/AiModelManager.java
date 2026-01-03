package com.smartwardrobeai.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartwardrobeai.mapper.SysAiModelMapper;
import com.smartwardrobeai.model.dto.AiExecutionDTO;
import com.smartwardrobeai.model.entity.SysAiModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * AI 模型管理器 (The Factory)
 * <p>
 * 核心职责：
 * 负责将 "前端参数" 与 "数据库配置" 进行合并，创建可执行的策略对象。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiModelManager {

    private final SysAiModelMapper sysAiModelMapper;
    private final RestTemplate restTemplate;

    /**
     * 创建 AI 分析策略
     *
     * @param dto 前端传递的执行参数
     * @return 组装好的策略对象
     */
    public AiAnalysisStrategy createStrategy(AiExecutionDTO dto) {
        // 1. 校验基础参数
        if (dto == null || dto.modelKey() == null) {
            throw new IllegalArgumentException("AI模型参数不能为空");
        }

        // 2. 从数据库查询模型配置 (获取关键的 URL 和 Key)
        SysAiModel dbModel = sysAiModelMapper.selectOne(
                new LambdaQueryWrapper<SysAiModel>()
                        .eq(SysAiModel::getModelKey, dto.modelKey())
                        .eq(SysAiModel::getStatus, 1) // 必须是启用的
        );

        if (dbModel == null) {
            log.error("模型不存在或已禁用: {}", dto.modelKey());
            throw new RuntimeException("指定的 AI 模型不存在或已下架");
        }

        // 3. 计算最终配置 (合并逻辑)
        AiModelConfig config = buildConfig(dbModel, dto);

        // 4. 实例化策略
        // 目前我们只有 OpenAI 兼容策略，如果以后接入文心一言SDK版，可以在这里 switch case
        return new OpenAiCompatibleStrategy(config, restTemplate);
    }

    /**
     * 内部私有方法：构建配置对象
     * 包含风控和参数合并逻辑
     */
    private AiModelConfig buildConfig(SysAiModel dbModel, AiExecutionDTO dto) {

        // --- 逻辑 A: 处理思考模式开关 ---
        boolean finalEnableThinking = false;
        // 只有当数据库允许(supportThinking=1) 且 用户前端要求开启(dto=true) 时，才开启
        if (Boolean.TRUE.equals(dbModel.getSupportThinking()) && Boolean.TRUE.equals(dto.enableThinking())) {
            finalEnableThinking = true;
        }

        // --- 逻辑 B: 处理 Token 预算 (含风控截断) ---
        Long finalBudget = 0L;
        if (finalEnableThinking) {
            // 优先用用户填的，没填用数据库默认
            Long userBudget = dto.thinkingBudget();
            Long defaultBudget = dbModel.getDefaultThinkingBudget() != null ? dbModel.getDefaultThinkingBudget() : 1024L;

            finalBudget = (userBudget != null && userBudget > 0) ? userBudget : defaultBudget;

            // 🛡️ 风控：如果数据库设置了上限(maxThinkingBudget)，则必须截断
            if (dbModel.getMaxThinkingBudget() != null && dbModel.getMaxThinkingBudget() > 0) {
                if (finalBudget > dbModel.getMaxThinkingBudget()) {
                    log.warn("用户请求Token [{}] 超过模型 [{}] 上限 [{}]，已自动截断",
                            finalBudget, dbModel.getModelName(), dbModel.getMaxThinkingBudget());
                    finalBudget = dbModel.getMaxThinkingBudget();
                }
            }
        }

        // --- 逻辑 C: 组装最终 Config ---
        return AiModelConfig.builder()
                // 核心凭证 (来自 DB)
                .modelName(dbModel.getModelName())
                .baseUrl(dbModel.getBaseUrl())
                .apiKey(dbModel.getApiKey())
                // 动态参数 (来自 计算结果)
                .finalThinkingEnabled(finalEnableThinking)
                .finalThinkingBudget(finalBudget)
                .build();
    }
}