package com.smartwardrobeai.ai;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI 分析策略接口 (The Gun)
 * <p>
 * 核心逻辑：
 * 无论底层是 阿里云Qwen、OpenAI、还是 DeepSeek，
 * 只要实现了这个接口，就能被业务层调用。
 * </p>
 */
public interface AiAnalysisStrategy {

    /**
     * 执行图片分析
     *
     * @param file 前端上传的图片文件
     * @return AI 返回的原始 JSON 数据 (业务层后续再解析成 VO)
     */
    JSONObject analyze(MultipartFile file);
}