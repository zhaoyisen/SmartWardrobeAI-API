package com.smartwardrobeai.app.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.smartwardrobeai.admin.model.entity.SysCategoryStrategy;
import com.smartwardrobeai.admin.service.SysCategoryStrategyService;
import com.smartwardrobeai.app.model.vo.ClothingAnalysisVO;
import com.smartwardrobeai.app.service.DictService;
import com.smartwardrobeai.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private final SysCategoryStrategyService categoryStrategyService;
    private final DictService dictService;

    /**
     * 构造函数 (由工厂调用)
     * 在这里接收配置，并"记住"它
     */
    public OpenAiCompatibleStrategy(AiModelConfig config, RestTemplate restTemplate, SysCategoryStrategyService categoryStrategyService, DictService dictService) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.categoryStrategyService = categoryStrategyService;
        this.dictService = dictService;
    }

    @Override
    public boolean detect(MultipartFile file) {
        try {
            // 1. 图片转 Base64 (Data URI Scheme)
            String base64Content = Base64.getEncoder().encodeToString(file.getBytes());
            String finalBase64Url = "data:" + file.getContentType() + ";base64," + base64Content;

            // 2. 构造检测请求 Body
            Map<String, Object> requestBody = buildDetectionRequestBody(finalBase64Url);

            // 3. 发起请求并解析结果
            String rawResponse = callAiApi(requestBody);
            JSONObject detectResult = parseResponse(rawResponse);

            // 4. 解析检测结果
            Boolean isClothing = detectResult.getBoolean("isClothing");
            Double confidence = detectResult.getDouble("confidence");
            String reason = detectResult.getString("reason");

            log.info("AI 衣物检测结果: isClothing={}, confidence={}, reason={}", isClothing, confidence, reason);

            // 5. 如果不是衣物，抛出业务异常
            if (!Boolean.TRUE.equals(isClothing)) {
                String errorMessage = "请上传衣物图片";
                if (reason != null && !reason.trim().isEmpty()) {
                    errorMessage += "：" + reason;
                }
                if (confidence != null) {
                    errorMessage += String.format(" (置信度: %.2f)", confidence);
                }
                throw new BusinessException(errorMessage);
            }

            // 6. 检测通过，返回 true
            return true;

        } catch (BusinessException e) {
            // 业务异常直接抛出
            throw e;
        } catch (Exception e) {
            log.error("AI 衣物检测失败: Model={}, Error={}", config.getModelName(), e.getMessage(), e);
            throw new RuntimeException("AI 衣物检测失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean detect(String imageUrl) {
        try {
            log.info("使用图片URL进行检测: {}", imageUrl);

            // 1. 构造检测请求 Body（直接使用URL，无需Base64编码）
            Map<String, Object> requestBody = buildDetectionRequestBody(imageUrl);

            // 2. 发起请求并解析结果
            String rawResponse = callAiApi(requestBody);
            JSONObject detectResult = parseResponse(rawResponse);

            // 3. 解析检测结果
            Boolean isClothing = detectResult.getBoolean("isClothing");
            Double confidence = detectResult.getDouble("confidence");
            String reason = detectResult.getString("reason");

            log.info("AI 衣物检测结果: isClothing={}, confidence={}, reason={}", isClothing, confidence, reason);

            // 4. 如果不是衣物，抛出业务异常
            if (!Boolean.TRUE.equals(isClothing)) {
                String errorMessage = "请上传衣物图片";
                if (reason != null && !reason.trim().isEmpty()) {
                    errorMessage += "：" + reason;
                }
                if (confidence != null) {
                    errorMessage += String.format(" (置信度: %.2f)", confidence);
                }
                throw new BusinessException(errorMessage);
            }

            // 5. 检测通过，返回 true
            return true;

        } catch (BusinessException e) {
            // 业务异常直接抛出
            throw e;
        } catch (Exception e) {
            log.error("AI 衣物检测失败: Model={}, URL={}, Error={}", config.getModelName(), imageUrl, e.getMessage(), e);
            throw new RuntimeException("AI 衣物检测失败: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] removeBackground(MultipartFile file) {
        try {
            // 1. 图片转 Base64 (Data URI Scheme)
            String base64Content = Base64.getEncoder().encodeToString(file.getBytes());
            String finalBase64Url = "data:" + file.getContentType() + ";base64," + base64Content;

            // 2. 构造背景去除请求 Body
            Map<String, Object> requestBody = buildBackgroundRemovalRequestBody(finalBase64Url);

            // 3. 发起请求并解析结果
            String rawResponse = callAiApi(requestBody);

            // 4. 从响应中提取Base64编码的图片
            return extractImageFromResponse(rawResponse);

        } catch (Exception e) {
            log.error("AI 背景去除失败: Model={}, Error={}", config.getModelName(), e.getMessage(), e);
            throw new RuntimeException("AI 背景去除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ClothingAnalysisVO analyze(MultipartFile file, Long imageId, String imageUrl) {
        try {
            // 1. 图片转 Base64 (Data URI Scheme)
            String base64Content = Base64.getEncoder().encodeToString(file.getBytes());
            String finalBase64Url = "data:" + file.getContentType() + ";base64," + base64Content;

            // 2. 构造请求 Body (直接使用 this.config)
            Map<String, Object> requestBody = buildRequestBody(finalBase64Url);

            // 3. 发起请求并解析结果
            String rawResponse = callAiApi(requestBody);
            JSONObject aiData = parseResponse(rawResponse);

            // 4. 匹配本地分类策略（从数据库查询，带缓存）
            String category = aiData.getString("category");
            SysCategoryStrategy catStrategy = categoryStrategyService.match(category);

            log.info("AI分析完成: category={}, color={}, region={}", 
                    category, aiData.getString("color"), catStrategy.getRegion());

            // 5. 组装并返回完整的 ClothingAnalysisVO
            return new ClothingAnalysisVO(
                    imageId,
                    imageUrl,
                    null, // maskImageId - 暂时不使用去背景图片
                    null, // maskImageUrl - 暂时不使用去背景图片
                    catStrategy.getCategoryCode(),
                    catStrategy.getRegion(),
                    Integer.valueOf(catStrategy.getLayer()),
                    aiData.getString("color"),
                    aiData.getString("season"),
                    aiData.getString("fitType"),
                    aiData.getString("viewType")
            );

        } catch (Exception e) {
            log.error("AI 策略执行失败: Model={}, Error={}", config.getModelName(), e.getMessage(), e);
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 统一的AI API调用方法
     */
    private String callAiApi(Map<String, Object> requestBody) {
        // 1. 构造 Header (直接使用 this.config.getApiKey)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getApiKey());

        // 2. 拼接 URL
        String requestUrl = config.getBaseUrl();
        if (!requestUrl.endsWith("/")) requestUrl += "/";
        requestUrl += "chat/completions";

        log.info("正在调用 AI 模型: [{}], URL: [{}]", config.getModelName(), requestUrl);

        // 3. 发起 HTTP POST
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        // 先接收 String 原始响应，方便调试和容错
        return restTemplate.postForObject(requestUrl, entity, String.class);
    }

    /**
     * 构造检测请求体
     * <p>
     * 支持两种格式：
     * 1. Base64 Data URI: "data:image/jpeg;base64,..."（原有方式）
     * 2. 普通 HTTP/HTTPS URL: "http://example.com/image.jpg"（优化方式）
     * </p>
     *
     * @param imageUrlOrBase64 图片URL或Base64 Data URI
     */
    private Map<String, Object> buildDetectionRequestBody(String imageUrlOrBase64) {
        List<Map<String, Object>> contentList = new ArrayList<>();

        // --- 图片 ---
        // OpenAI 兼容协议支持直接使用 HTTP/HTTPS URL，也支持 Base64 Data URI
        Map<String, Object> imgMap = new HashMap<>();
        imgMap.put("url", imageUrlOrBase64);
        contentList.add(Map.of("type", "image_url", "image_url", imgMap));

        // --- 检测 Prompt ---
        String prompt = """
                你是一个图像识别专家。请严格验证图片是否符合试穿图生成的要求。
                
                验证标准（必须全部满足，否则返回 isClothing=false）：
                1. 图片中必须只包含一件衣物（不允许多件衣物）
                2. 不允许有其他物品、人物或复杂背景
                3. 图片应该是纯衣物展示，适合用于后续的试穿图生成
                
                如果不符合要求，请在 reason 字段中详细说明原因，例如：
                - "图片中包含多件衣物"
                - "图片中包含人物或其他物品"
                - "背景过于复杂，不适合试穿图生成"
                - "图片中未检测到衣物"
                
                严格返回以下 JSON 格式，不要 Markdown：
                {
                    "isClothing": true/false,
                    "confidence": 0.0-1.0,
                    "reason": "详细说明验证结果和原因"
                }
                """;
        contentList.add(Map.of("type", "text", "text", prompt));

        // --- Message ---
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", contentList);

        // --- Final Body ---
        Map<String, Object> body = new HashMap<>();
        body.put("model", config.getModelName());
        body.put("messages", Collections.singletonList(userMessage));
        body.put("stream", false);

        // 检测任务不需要思考模式，保持简洁
        return body;
    }

    /**
     * 构造背景去除请求体
     */
    private Map<String, Object> buildBackgroundRemovalRequestBody(String base64Url) {
        List<Map<String, Object>> contentList = new ArrayList<>();

        // --- 图片 ---
        Map<String, Object> imgMap = new HashMap<>();
        imgMap.put("url", base64Url);
        contentList.add(Map.of("type", "image_url", "image_url", imgMap));

        // --- 背景去除 Prompt ---
        String prompt = """
                你是一个图像处理专家。请将图片中的衣物部分提取出来，去除所有背景，保留衣物主体。
                返回去除背景后的透明背景PNG图片（Base64编码），格式为：
                {
                    "image": "data:image/png;base64,..."
                }
                注意：请确保返回的是完整的Base64编码的PNG图片数据。
                """;
        contentList.add(Map.of("type", "text", "text", prompt));

        // --- Message ---
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", contentList);

        // --- Final Body ---
        Map<String, Object> body = new HashMap<>();
        body.put("model", config.getModelName());
        body.put("messages", Collections.singletonList(userMessage));
        body.put("stream", false);

        // 背景去除任务可能需要更多Token，但不启用思考模式
        return body;
    }

    /**
     * 从AI响应中提取Base64编码的图片
     */
    private byte[] extractImageFromResponse(String rawResponse) {
        try {
            JSONObject jsonResponse = JSON.parseObject(rawResponse);
            if (jsonResponse == null || !jsonResponse.containsKey("choices")) {
                log.error("AI 响应格式异常: 无 choices 字段。原始响应: {}", rawResponse.length() > 500 ? rawResponse.substring(0, 500) + "..." : rawResponse);
                throw new RuntimeException("AI 响应格式异常: 无 choices 字段");
            }

            // contentStr 是一个转义的JSON字符串，需要先解析
            String contentStr = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            log.debug("AI返回的原始contentStr长度: {} 字符", contentStr != null ? contentStr.length() : 0);
            
            if (contentStr == null || contentStr.trim().isEmpty()) {
                throw new RuntimeException("AI 返回的content为空");
            }

            // 方法1: 优先尝试解析contentStr为JSON（因为它是标准的JSON字符串）
            String imageBase64 = extractBase64FromJsonContent(contentStr);
            
            // 方法2: 如果JSON解析失败，尝试使用正则提取（应对格式异常或截断的情况）
            if (imageBase64 == null || imageBase64.isEmpty()) {
                log.warn("JSON解析方式提取Base64失败，尝试正则提取方式。contentStr长度: {}", contentStr.length());
                imageBase64 = extractBase64FromContent(contentStr);
            }

            if (imageBase64 == null || imageBase64.isEmpty()) {
                log.error("无法从AI响应中提取图片Base64数据。contentStr长度: {}, 预览: {}", 
                    contentStr.length(),
                    contentStr.length() > 1000 ? contentStr.substring(0, 1000) + "..." : contentStr);
                throw new RuntimeException("AI 返回的图片数据为空或格式异常。可能是响应被截断，请检查AI模型的输出限制。");
            }

            log.info("成功提取到Base64数据，原始长度: {}", imageBase64.length());

            // 处理Data URI格式: data:image/png;base64,...
            String pureBase64 = imageBase64;
            if (imageBase64.startsWith("data:")) {
                int commaIndex = imageBase64.indexOf(',');
                if (commaIndex > 0 && commaIndex < imageBase64.length() - 1) {
                    pureBase64 = imageBase64.substring(commaIndex + 1);
                }
            }

            // 清理Base64字符串：去除所有空白字符
            pureBase64 = pureBase64.replaceAll("\\s", "");
            
            log.info("提取到Base64图片数据，长度: {} 字符", pureBase64.length());

            // 验证并修复Base64字符串（处理截断问题）
            pureBase64 = fixBase64Padding(pureBase64);
            
            if (pureBase64.isEmpty()) {
                throw new RuntimeException("Base64字符串为空或无效");
            }

            // Base64解码为字节数组
            try {
                return Base64.getDecoder().decode(pureBase64);
            } catch (IllegalArgumentException e) {
                log.error("Base64解码失败，字符串长度: {}, 前100字符: {}", 
                    pureBase64.length(), pureBase64.length() > 100 ? pureBase64.substring(0, 100) : pureBase64);
                throw new RuntimeException("Base64解码失败: 数据可能被截断或不完整 - " + e.getMessage(), e);
            }

        } catch (RuntimeException e) {
            throw e; // 重新抛出运行时异常
        } catch (Exception e) {
            log.error("提取图片Base64失败: {}", e.getMessage(), e);
            throw new RuntimeException("提取图片Base64失败: " + e.getMessage(), e);
        }
    }

    /**
     * 修复Base64字符串的padding（处理可能被截断的情况）
     * Base64字符串长度必须是4的倍数，如果不是，需要添加padding或截断
     */
    private String fixBase64Padding(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return "";
        }

        // 去除末尾可能存在的无效字符
        base64 = base64.replaceAll("[^A-Za-z0-9+/=]", "");

        int length = base64.length();
        int remainder = length % 4;

        if (remainder == 0) {
            // 长度正确，直接返回
            return base64;
        }

        // 如果长度不是4的倍数，可能需要添加padding或截断
        // 但是，如果数据被截断，添加padding可能导致解码出错误的数据
        // 所以这里我们尝试修复，但会记录警告
        log.warn("Base64字符串长度不是4的倍数，当前长度: {}, 余数: {}, 尝试修复", length, remainder);
        
        // 尝试截断到最接近的4的倍数（向下取整）
        int truncatedLength = (length / 4) * 4;
        if (truncatedLength > 0) {
            String truncated = base64.substring(0, truncatedLength);
            log.warn("Base64字符串可能被截断，使用截断后的长度: {}", truncatedLength);
            return truncated;
        }

        return base64; // 返回原始字符串，让解码器报告具体错误
    }

    /**
     * 从contentStr（转义的JSON字符串）中解析并提取Base64
     */
    private String extractBase64FromJsonContent(String contentStr) {
        try {
            // contentStr 本身是一个JSON字符串，需要先解析
            // 例如: "{\n    \"image\": \"data:image/png;base64,...\"\n}" 或 "{\n    \"image\": \"iVBORw0KG...\"\n}"
            JSONObject contentJson = JSON.parseObject(contentStr);
            
            if (contentJson != null && contentJson.containsKey("image")) {
                String imageValue = contentJson.getString("image");
                if (imageValue != null && !imageValue.trim().isEmpty()) {
                    log.info("从JSON中成功提取到image字段，长度: {} 字符", imageValue.length());
                    return imageValue.trim();
                } else {
                    log.warn("JSON中image字段存在但值为空");
                }
            } else {
                log.warn("JSON中未找到image字段。contentStr长度: {}, 预览: {}", 
                    contentStr != null ? contentStr.length() : 0,
                    contentStr != null && contentStr.length() > 300 ? contentStr.substring(0, 300) + "..." : contentStr);
            }
            
            return null;
        } catch (Exception e) {
            log.warn("解析contentStr为JSON失败: {}, contentStr长度: {}", 
                e.getMessage(), 
                contentStr != null ? contentStr.length() : 0);
            // JSON解析失败，可能格式异常或被截断，返回null让调用方使用正则提取
            return null;
        }
    }

    /**
     * 从内容中直接提取Base64字符串（使用正则，作为JSON解析失败时的备用方案）
     * 匹配格式: "image": "data:image/png;base64,..." 或 "image": "纯Base64字符串"
     * 注意：content可能包含转义字符（如 \n），Base64可能被截断
     */
    private String extractBase64FromContent(String content) {
        if (content == null || content.isEmpty()) {
            log.warn("extractBase64FromContent: content为空");
            return null;
        }

        log.debug("开始正则提取Base64，content长度: {}", content.length());

        // 模式1: 匹配 "image": "纯Base64字符串"（有结束引号的情况）
        Pattern pattern1 = Pattern.compile("\"image\"\\s*:\\s*\"([A-Za-z0-9+/=\\s]+)\"", Pattern.DOTALL | Pattern.MULTILINE);
        Matcher matcher1 = pattern1.matcher(content);
        if (matcher1.find()) {
            String base64 = matcher1.group(1);
            if (base64 != null) {
                base64 = base64.replaceAll("\\s", ""); // 去除空白字符
                if (!base64.isEmpty()) {
                    log.info("使用模式1提取到Base64（有结束引号），长度: {}", base64.length());
                    return base64;
                }
            }
        }
        
        // 模式1.5: 匹配 "image": "纯Base64字符串"（没有结束引号的情况，被截断）
        // 匹配从 "image": " 开始到字符串末尾的所有Base64字符
        Pattern pattern1_5 = Pattern.compile("\"image\"\\s*:\\s*\"([A-Za-z0-9+/=\\s]+)(?:[^A-Za-z0-9+/=\\s]|$)", Pattern.DOTALL | Pattern.MULTILINE);
        Matcher matcher1_5 = pattern1_5.matcher(content);
        if (matcher1_5.find()) {
            String base64 = matcher1_5.group(1);
            if (base64 != null) {
                base64 = base64.replaceAll("\\s", ""); // 去除空白字符
                if (!base64.isEmpty() && base64.length() >= 50) { // 至少50个字符才认为有效
                    log.warn("使用模式1.5提取到Base64（无结束引号，可能被截断），长度: {}", base64.length());
                    return base64;
                }
            }
        }

        // 模式2: 匹配 "image": "data:image/png;base64,xxxxx"（Data URI格式）
        Pattern pattern2 = Pattern.compile("\"image\"\\s*:\\s*\"data:image/[^;]+;base64,([^\"]+)\"", Pattern.DOTALL | Pattern.MULTILINE);
        Matcher matcher2 = pattern2.matcher(content);
        if (matcher2.find()) {
            String base64 = matcher2.group(1).replaceAll("\\s", ""); // 去除空白字符
            if (!base64.isEmpty()) {
                log.info("使用模式2提取到Base64（Data URI格式），长度: {}", base64.length());
                return "data:image/png;base64," + base64;
            }
        }

        // 模式3: 匹配可能被截断的JSON（没有结束引号的情况，匹配到行尾或非Base64字符）
        // 例如: "image": "iVBORw0KG... 后面没有引号，直接到文件末尾
        Pattern pattern3 = Pattern.compile("\"image\"\\s*:\\s*\"([A-Za-z0-9+/=\\s]+?)(?:\"|\\n|\\r|$)", Pattern.DOTALL | Pattern.MULTILINE);
        Matcher matcher3 = pattern3.matcher(content);
        if (matcher3.find()) {
            String base64 = matcher3.group(1).replaceAll("\\s", ""); // 去除空白字符
            if (!base64.isEmpty() && base64.length() >= 50) {
                log.warn("使用模式3提取到Base64（截断的JSON格式），长度: {}", base64.length());
                return base64;
            }
        }
        
        // 模式3.5: 更宽松的匹配，匹配 "image": 后面直到第一个非Base64字符或字符串末尾的所有内容
        Pattern pattern3_5 = Pattern.compile("\"image\"\\s*:\\s*\"([^\"]*)", Pattern.DOTALL | Pattern.MULTILINE);
        Matcher matcher3_5 = pattern3_5.matcher(content);
        if (matcher3_5.find()) {
            String candidate = matcher3_5.group(1);
            if (candidate != null) {
                // 只保留Base64字符
                String base64 = candidate.replaceAll("[^A-Za-z0-9+/=]", "").replaceAll("\\s", "");
                if (!base64.isEmpty() && base64.length() >= 50) {
                    log.warn("使用模式3.5提取到Base64（宽松匹配），长度: {}", base64.length());
                    return base64;
                }
            }
        }

        // 模式4: 匹配 base64, 后面的Base64字符串（应对特殊的截断情况）
        Pattern pattern4 = Pattern.compile("base64,([A-Za-z0-9+/=\\s]{100,})", Pattern.DOTALL | Pattern.MULTILINE);
        Matcher matcher4 = pattern4.matcher(content);
        if (matcher4.find()) {
            String base64 = matcher4.group(1).replaceAll("\\s", ""); // 去除空白字符
            if (!base64.isEmpty()) {
                log.info("使用模式4提取到Base64（base64,格式），长度: {}", base64.length());
                return base64;
            }
        }

        // 模式5: 尝试查找最长的Base64字符串（最后的兜底方案）
        // 降低最小长度要求，因为Base64可能被截断
        Pattern pattern5 = Pattern.compile("([A-Za-z0-9+/=]{100,})", Pattern.DOTALL);
        Matcher matcher5 = pattern5.matcher(content);
        String longestBase64 = null;
        int maxLength = 0;
        while (matcher5.find()) {
            String candidate = matcher5.group(1).replaceAll("\\s", "");
            if (candidate.length() > maxLength) {
                maxLength = candidate.length();
                longestBase64 = candidate;
            }
        }
        if (longestBase64 != null && longestBase64.length() >= 100) {
            log.info("使用模式5提取到Base64（最长字符串），长度: {}", longestBase64.length());
            return longestBase64;
        }

        log.warn("所有正则模式都未匹配到Base64字符串。content预览: {}", 
            content.length() > 500 ? content.substring(0, 500) + "..." : content);
        return null;
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
        // 从数据库动态获取品类列表
        String categoryList = getCategoryListForPrompt();
        // 从字典获取选项列表
        String colorOptions = getDictOptionsForPrompt("clothing_color", "主色调英文");
        String seasonOptions = getDictOptionsForPrompt("clothing_season", "适用季节英文");
        String fitTypeOptions = getDictOptionsForPrompt("clothing_fit_type", "Regular/Loose/Slim/Oversize");
        String viewTypeOptions = getDictOptionsForPrompt("clothing_view_type", "Flat/Model/Hanger");
        
        String prompt = String.format("""
                你是一个时尚专家。请分析图中的衣物。
                严格返回以下 JSON 格式，不要 Markdown：
                {
                    "category": "请从列表选择: %s",
                    "color": "请从列表选择: %s",
                    "season": "请从列表选择: %s",
                    "fitType": "请从列表选择: %s",
                    "viewType": "请从列表选择: %s"
                }
                """, categoryList, colorOptions, seasonOptions, fitTypeOptions, viewTypeOptions);
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

        String contentStr = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

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

    /**
     * 从数据库获取品类列表，用于构建 AI Prompt
     *
     * @return 品类代码列表，用逗号分隔（如 "T-shirt, Shirt, Hoodie, ..."）
     */
    private String getCategoryListForPrompt() {
        try {
            List<SysCategoryStrategy> strategies = categoryStrategyService.getAllEnabled();
            // 过滤掉 Unknown，只返回实际品类
            return strategies.stream().filter(s -> !"Unknown".equalsIgnoreCase(s.getCategoryCode())).map(SysCategoryStrategy::getCategoryCode).collect(Collectors.joining(", "));
        } catch (Exception e) {
            log.error("获取品类列表失败，使用默认列表", e);
            // 降级处理：返回硬编码的默认列表
            return "T-shirt, Shirt, Hoodie, Sweater, Jacket, Coat, Jeans, Pants, Shorts, Skirt, Dress, Sneakers, Boots, Hat, Bag";
        }
    }

    /**
     * 从字典获取选项列表，用于构建 AI Prompt
     * 优先使用 promptText（如果存在且不为空），否则使用 value
     *
     * @param dictType 字典类型编码（如：clothing_color）
     * @param defaultValue 降级处理时的默认值
     * @return 选项列表，用逗号分隔（如 "red, blue, green"）
     */
    private String getDictOptionsForPrompt(String dictType, String defaultValue) {
        try {
            List<Map<String, String>> dictList = dictService.getDictByType(dictType);
            if (dictList == null || dictList.isEmpty()) {
                log.warn("字典类型 [{}] 数据为空，使用默认值: {}", dictType, defaultValue);
                return defaultValue;
            }

            return dictList.stream()
                    .map(item -> {
                        // 优先使用 promptText，如果为空则使用 value
                        String promptText = item.get("promptText");
                        String value = item.get("value");
                        if (promptText != null && !promptText.trim().isEmpty()) {
                            // promptText 可能包含多个值（用逗号分隔），只取第一个
                            String[] parts = promptText.split(",");
                            return parts[0].trim();
                        }
                        return value != null ? value : "";
                    })
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            log.error("获取字典类型 [{}] 数据失败，使用默认值: {}", dictType, defaultValue, e);
            return defaultValue;
        }
    }
}