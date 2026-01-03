package com.smartwardrobeai.app.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用 OpenAI 协议兼容策略实现 (The Real Gun)
 * <p>
 * 核心逻辑：
 * 这是一个 "有状态" 的对象。
 * 它在被工厂创建时，就已经通过构造函数绑定了具体的 Config (Key, URL, Token预算等)。
 * 所以在调用 analyze 时，不需要再传配置。
 * </p>
 */
@Slf4j
public class OpenAiCompatibleStrategy implements AiAnalysisStrategy {

    // ✅ 配置被存储在这里，作为成员变量
    private final AiModelConfig config;
    private final RestTemplate restTemplate;

    /**
     * 构造函数 (由工厂调用)
     * 在这里接收配置，并"记住"它
     */
    public OpenAiCompatibleStrategy(AiModelConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    @Override
    public JSONObject analyze(MultipartFile file) {
        try {
            // 1. 图片转 Base64 (Data URI Scheme)
            String base64Content = Base64.getEncoder().encodeToString(file.getBytes());
            String finalBase64Url = "data:" + file.getContentType() + ";base64," + base64Content;

            // 2. 构造请求 Body (直接使用 this.config)
            Map<String, Object> requestBody = buildRequestBody(finalBase64Url);

            // 3. 构造 Header (直接使用 this.config.getApiKey)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            // 4. 拼接 URL
            String requestUrl = config.getBaseUrl();
            if (!requestUrl.endsWith("/")) requestUrl += "/";
            requestUrl += "chat/completions";

            log.info("正在调用 AI 模型: [{}], URL: [{}]", config.getModelName(), requestUrl);

            // 5. 发起 HTTP POST
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            // 先接收 String 原始响应，方便调试和容错
            String rawResponse = restTemplate.postForObject(requestUrl, entity, String.class);

            // 6. 解析结果 (含 JSON 提取逻辑)
            return parseResponse(rawResponse);

        } catch (Exception e) {
            log.error("AI 策略执行失败: Model={}, Error={}", config.getModelName(), e.getMessage(), e);
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage());
        }
    }

    /**
     * 构造请求体 (私有辅助方法)
     */
    private Map<String, Object> buildRequestBody(String base64Url) {
        List<Map<String, Object>> contentList = new ArrayList<>();

        // --- 图片 ---
        Map<String, Object> imgMap = new HashMap<>();
        imgMap.put("url", base64Url);
        contentList.add(Map.of("type", "image_url", "image_url", imgMap));

        // --- Prompt ---
        String prompt = """
            你是一个时尚专家。请分析图中的衣物。
            严格返回以下 JSON 格式，不要 Markdown：
            {
                "category": "请从列表选择: T-shirt, Shirt, Hoodie, Sweater, Jacket, Coat, Jeans, Pants, Shorts, Skirt, Dress, Sneakers, Boots, Hat, Bag",
                "color": "主色调英文",
                "season": "适用季节英文",
                "fitType": "Regular/Loose/Slim/Oversize",
                "viewType": "Flat/Model/Hanger"
            }
            """;
        contentList.add(Map.of("type", "text", "text", prompt));

        // --- Message ---
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", contentList);

        // --- Final Body ---
        Map<String, Object> body = new HashMap<>();
        // ✅ 使用配置中的真实模型 ID (如 qwen-vl-plus)
        body.put("model", config.getModelName());
        body.put("messages", Collections.singletonList(userMessage));
        // 后端同步调用，关闭流式
        body.put("stream", false);

        // ✅ 动态处理 Thinking 模式 (核心逻辑)
        if (config.isFinalThinkingEnabled()) {
            body.put("enable_thinking", true);
            // ✅ 使用配置中的 Token 预算 (Long)
            body.put("thinking_budget", config.getFinalThinkingBudget());
        }

        return body;
    }

    /**
     * 解析响应体 (私有辅助方法)
     */
    private JSONObject parseResponse(String rawResponse) {
        JSONObject jsonResponse = JSON.parseObject(rawResponse);
        if (jsonResponse == null || !jsonResponse.containsKey("choices")) {
            throw new RuntimeException("AI 响应格式异常: 无 choices 字段");
        }

        String contentStr = jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

        // 提取 JSON (应对思考过程的干扰)
        String cleanJson = extractJsonBlock(contentStr);
        log.debug("AI 解析结果(清洗后): {}", cleanJson);

        return JSON.parseObject(cleanJson);
    }

    /**
     * 工具：从混合文本中提取 JSON 代码块
     * 应对 AI 输出: <thinking>...</thinking> ```json {...} ```
     */
    private String extractJsonBlock(String text) {
        if (text == null) return "{}";
        text = text.trim();

        // 1. 尝试正则提取 markdown 代码块
        Pattern pattern = Pattern.compile("```json(.*?)```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) return matcher.group(1).trim();

        // 2. 尝试寻找最外层 {}
        int firstBrace = text.indexOf("{");
        int lastBrace = text.lastIndexOf("}");
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return text.substring(firstBrace, lastBrace + 1);
        }

        // 3. 兜底
        return text;
    }
}