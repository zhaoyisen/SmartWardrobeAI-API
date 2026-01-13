package com.smartwardrobeai.utils;

import com.smartwardrobeai.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * 上传衣物图，本地校验
 * 阶段一：硬性技术指标检测器
 * 作用：不调用 AI，利用数学规则毫秒级拦截劣质图片
 */
@Slf4j
@Component
public class TechnicalIndicatorValidator {

    // 允许的格式后缀
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp", "bmp");

    // 阈值定义
    private static final long MIN_SIZE_BYTES = 10 * 1024;    // 10KB (太小肯定没细节)
    private static final long MAX_SIZE_BYTES = 3 * 1024 * 1024; // 3MB (阿里云限制)

    private static final int MIN_DIMENSION = 512;  // 最小边长 (VTON 需要细节)
    private static final int MAX_DIMENSION = 3000; // 最大边长 (防止内存溢出)

    private static final double MIN_ASPECT_RATIO = 0.4; // 极瘦 (如 400x1000)
    private static final double MAX_ASPECT_RATIO = 2.5; // 极扁 (如 1000x400)

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 1. 基础 I/O 检查 (文件大小)
        checkFileSize(file);

        // 2. 扩展名预检查 (快速失败)
        checkExtension(file);

        // 3. 图像内容深度检查 (分辨率、宽高比)
        // 这一步会解码图片头部，消耗极少量 CPU，但非常有必要
        try (InputStream is = file.getInputStream()) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                // 能读流但解不出图片，说明文件已损坏或不是图片
                throw new BusinessException("文件已损坏或非图片格式");
            }
            checkDimensionsAndRatio(image);
        } catch (IOException e) {
            log.error("图片解码失败", e);
            throw new BusinessException("服务器无法读取该图片文件");
        }
    }

    private void checkFileSize(MultipartFile file) {
        long size = file.getSize();
        if (size < MIN_SIZE_BYTES) {
            throw new BusinessException("图片太小(<10KB)，无法识别衣物细节");
        }
        if (size > MAX_SIZE_BYTES) {
            throw new BusinessException("图片太大(>3MB)，请先压缩");
        }
    }

    private void checkExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) return;

        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException("不支持的图片格式: " + ext + "，请上传 JPG/PNG");
        }
    }

    private void checkDimensionsAndRatio(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // A. 分辨率检查
        if (width < MIN_DIMENSION || height < MIN_DIMENSION) {
            throw new BusinessException(String.format("图片分辨率过低 (%dx%d)，建议长宽均大于 %dpx", width, height, MIN_DIMENSION));
        }

        if (width > MAX_DIMENSION || height > MAX_DIMENSION) {
            throw new BusinessException(String.format("图片分辨率过高{}x{},建议长宽均小于{}px", width, height, MAX_DIMENSION));
        }

        // B. 宽高比检查 (防止长条图)
        double ratio = (double) width / height;
        if (ratio < MIN_ASPECT_RATIO || ratio > MAX_ASPECT_RATIO) {
            throw new BusinessException("图片比例异常，请上传接近正方形或竖屏的图片");
        }
    }
}