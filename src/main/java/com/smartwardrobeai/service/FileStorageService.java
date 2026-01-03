package com.smartwardrobeai.service;

import com.smartwardrobeai.model.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * 上传文件
     *
     * @param file 前端传来的文件
     * @return 文件的完整访问 URL
     */
    SysFile upload(MultipartFile file);
}