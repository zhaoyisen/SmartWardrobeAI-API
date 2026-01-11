package com.smartwardrobeai.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * 图片处理工具类
 * 提供图片格式转换、Base64编解码等常用方法
 */
@Slf4j
public class ImageUtils {

    /**
     * 将字节数组转换为 MultipartFile
     *
     * @param bytes    图片字节数组
     * @param filename 文件名（不含路径）
     * @return MultipartFile 对象
     */
    public static MultipartFile bytesToMultipartFile(byte[] bytes, String filename) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("字节数组不能为空");
        }
        if (filename == null || filename.trim().isEmpty()) {
            filename = "image.png";
        }

        // 根据文件名推断ContentType
        String contentType = determineContentType(filename);

        return bytesToMultipartFile(bytes, filename, contentType);
    }

    /**
     * 将字节数组转换为 MultipartFile（指定ContentType）
     *
     * @param bytes       图片字节数组
     * @param filename    文件名
     * @param contentType Content-Type（如 "image/png", "image/jpeg"）
     * @return MultipartFile 对象
     */
    public static MultipartFile bytesToMultipartFile(byte[] bytes, String filename, String contentType) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("字节数组不能为空");
        }
        if (filename == null || filename.trim().isEmpty()) {
            filename = "image.png";
        }
        if (contentType == null || contentType.trim().isEmpty()) {
            contentType = "image/png";
        }

        return new ByteArrayMultipartFile(bytes, filename, contentType);
    }

    /**
     * Base64字符串解码为字节数组
     * 支持标准Base64和Data URI格式（data:image/png;base64,...）
     *
     * @param base64 Base64编码的字符串
     * @return 解码后的字节数组
     */
    public static byte[] base64ToBytes(String base64) {
        if (base64 == null || base64.trim().isEmpty()) {
            throw new IllegalArgumentException("Base64字符串不能为空");
        }

        // 处理Data URI格式: data:image/png;base64,...
        String base64Data = base64.trim();
        if (base64Data.startsWith("data:")) {
            int commaIndex = base64Data.indexOf(',');
            if (commaIndex > 0 && commaIndex < base64Data.length() - 1) {
                base64Data = base64Data.substring(commaIndex + 1);
            }
        }

        try {
            return Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            log.error("Base64解码失败: {}", e.getMessage());
            throw new IllegalArgumentException("Base64字符串格式错误: " + e.getMessage(), e);
        }
    }

    /**
     * 字节数组编码为Base64字符串
     *
     * @param bytes 字节数组
     * @return Base64编码的字符串
     */
    public static String bytesToBase64(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("字节数组不能为空");
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 根据文件名推断ContentType
     *
     * @param filename 文件名
     * @return Content-Type字符串
     */
    private static String determineContentType(String filename) {
        if (filename == null) {
            return "image/png";
        }

        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFilename.endsWith(".webp")) {
            return "image/webp";
        } else {
            // 默认PNG格式（适用于去背景图片）
            return "image/png";
        }
    }

    /**
     * 自定义 MultipartFile 实现
     * 用于将字节数组转换为 MultipartFile，不依赖 spring-test
     */
    private static class ByteArrayMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String filename;
        private final String contentType;

        public ByteArrayMultipartFile(byte[] content, String filename, String contentType) {
            this.content = content;
            this.filename = filename;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return filename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content != null ? content.length : 0;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            throw new UnsupportedOperationException("transferTo is not supported");
        }
    }
}
