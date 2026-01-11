package com.smartwardrobeai.app.ai;

import com.smartwardrobeai.app.model.vo.ClothingAnalysisVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI 分析策略接口 (The Gun)
 * <p>
 * 核心逻辑：
 * 无论底层是 阿里云Qwen、OpenAI、还是 DeepSeek，
 * 只要实现了这个接口，就能被业务层调用。
 * </p>
 * <p>
 * 接口设计原则：
 * 1. detect 方法返回 boolean，检测失败时抛出 BusinessException（包含详细错误信息）
 * 2. analyze 方法返回完整的 ClothingAnalysisVO，包含所有业务逻辑组装结果
 * </p>
 */
public interface AiAnalysisStrategy {

    /**
     * 检测图片中是否包含衣物
     *
     * @param file 前端上传的图片文件
     * @return true 如果图片中包含衣物，false 如果未包含（但通常会抛出异常）
     * @throws com.smartwardrobeai.common.BusinessException 如果检测到非衣物图片，会抛出异常并包含原因信息
     */
    boolean detect(MultipartFile file);

    /**
     * 去除图片背景，提取衣物主体
     *
     * @param file 前端上传的图片文件
     * @return 去除背景后的 PNG 格式字节数组（透明背景）
     */
    byte[] removeBackground(MultipartFile file);

    /**
     * 执行图片分析并返回完整结果
     *
     * @param file     前端上传的图片文件
     * @param imageId  原始图片的ID（用于组装返回结果）
     * @param imageUrl 原始图片的URL（用于组装返回结果）
     * @return 完整的 AI 分析结果 VO，包含所有识别出的属性信息
     */
    ClothingAnalysisVO analyze(MultipartFile file, Long imageId, String imageUrl);
}