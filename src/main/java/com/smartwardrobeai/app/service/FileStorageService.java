package com.smartwardrobeai.app.service;

import com.smartwardrobeai.app.model.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * 上传文件
     *
     * @param file 前端传来的文件
     * @return 文件的完整访问 URL
     */
    SysFile upload(MultipartFile file);

    /**
     * 从 URL 下载并上传文件
     *
     * @param url 文件的 URL 地址
     * @param originalFileName 原始文件名
     * @return 上传后的文件信息
     */
    SysFile uploadFromUrl(String url, String originalFileName);
}