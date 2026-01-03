package com.smartwardrobeai.app.ai;

import lombok.Builder;
import lombok.Data;

/**
 * AI 模型最终执行配置 (Internal Config)
 * <p>
 * 核心逻辑说明：
 * 这是一个 "传输对象 (DTO)"，但它只在后端内部流转。
 * 它代表了 **策略工厂 (Factory)** 经过复杂的计算（数据库默认值 + 用户前端参数覆盖 + 风控截断）后，
 * 产出的 **最终决定**。
 * </p>
 * <p>
 * 职责边界：
 * 1. 策略类 (Strategy) **完全信任** 本对象中的所有参数，不再做校验或逻辑判断。
 * 2. 如果本对象里 enableThinking=true，策略类就必须开启思考模式。
 * </p>
 */
@Data
@Builder
public class AiModelConfig {

    // =========================================================
    // 1. 基础连接参数 (来自数据库)
    // =========================================================

    /**
     * 实际调用的模型名称 (Model ID)
     * <p>
     * 例如: "qwen-vl-plus", "qwen-vl-max"
     * 注意：这不同于前端传的 modelKey ("qwen-plus")，这是发给阿里云/OpenAI 的真实 ID。
     * </p>
     */
    private String modelName;

    /**
     * 接口 Base URL
     * <p>
     * 例如: "https://dashscope.aliyuncs.com/compatible-mode/v1"
     * 即使是同一家供应商，不同版本的模型 URL 也可能不同，所以必须动态配置。
     * </p>
     */
    private String baseUrl;

    /**
     * 鉴权 API Key
     * <p>
     * 极其敏感的信息，严禁暴露给前端。
     * 由工厂从数据库解密/读取后填入。
     * </p>
     */
    private String apiKey;

    // =========================================================
    // 2. 动态控制参数 (来自 用户意图 + 数据库默认值 + 风控计算)
    // =========================================================

    /**
     * 最终是否开启思考模式 (Thinking Mode)
     * <p>
     * 计算逻辑：
     * IF (模型不支持) -> False
     * ELSE IF (用户传了值) -> 使用用户的值
     * ELSE -> 使用数据库配置的默认值
     * </p>
     */
    private boolean finalThinkingEnabled;

    /**
     * 最终思考预算 Token 数 (Thinking Budget)
     * <p>
     * 计算逻辑：
     * 1. 优先取用户前端传递的值。
     * 2. 若用户未传，取数据库默认值。
     * 3. **风控截断**：最终值不能超过数据库配置的 max_thinking_budget。
     * </p>
     * 类型使用 Long 以适配未来超大窗口模型。
     */
    private Long finalThinkingBudget;
}