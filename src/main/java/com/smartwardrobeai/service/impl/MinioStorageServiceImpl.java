package com.smartwardrobeai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartwardrobeai.common.BusinessException;
import com.smartwardrobeai.common.UserContext;
import com.smartwardrobeai.config.MinioConfig;
import com.smartwardrobeai.mapper.SysFileMapper;
import com.smartwardrobeai.model.entity.SysFile;
import com.smartwardrobeai.service.FileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Primary // 标记为首选实现类，替代原有的七牛云实现
public class MinioStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final SysFileMapper sysFileMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysFile upload(MultipartFile file) {
        try {
            // 【新增步骤 1】计算文件 MD5 指纹
            // 注意：getInputStream() 只能读一次，算完 MD5 指针就到底了，后面上传还要读
            // 所以小文件可以直接读，大文件可能需要处理流复用，这里针对图片场景直接读没问题
            String fileHash = DigestUtils.md5DigestAsHex(file.getInputStream());

            // 【新增步骤 2】去数据库查有没有这个指纹
            SysFile existingFile = sysFileMapper.selectOne(
                    new LambdaQueryWrapper<SysFile>().eq(SysFile::getFileHash, fileHash)
            );

            // 【新增步骤 3】如果存在，直接返回旧数据（实现秒传）
            if (existingFile != null) {
                log.info("检测到重复文件，触发秒传: {}", existingFile.getFileUrl());
                return existingFile;
            }


            // 1. 生成存储路径 (例如: 2026/01/02/uuid.png)
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String fileName = datePath + "/" + UUID.randomUUID().toString().replace("-", "") + suffix;

            // 2. 上传文件到 MinIO
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .object(fileName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType()) // 关键：设置 ContentType 浏览器才能预览
                                .build()
                );
            }

            // 3. 构建可访问的 URL
            // 格式：http://localhost:9000/桶名/路径
            String fileUrl = String.format("%s/%s/%s",
                    minioConfig.getEndpoint(),
                    minioConfig.getBucketName(),
                    fileName);

            // 4. 保存文件记录到数据库
            SysFile sysFile = new SysFile();
            sysFile.setFileName(originalFilename);
            sysFile.setFilePath(fileName);
            sysFile.setFileUrl(fileUrl);
            sysFile.setFileSize(file.getSize());
            sysFile.setFileType(suffix);
            sysFile.setPlatform("minio");
            sysFile.setCreateBy(String.valueOf(UserContext.getUserId())); // 获取当前登录用户
            // 【关键】保存 MD5 到数据库
            sysFile.setFileHash(fileHash);

            sysFileMapper.insert(sysFile);

            log.info("文件上传 MinIO 成功: {}", fileUrl);
            return sysFile;

        } catch (Exception e) {
            log.error("MinIO 上传异常: ", e);
            throw new BusinessException("文件上传失败，请稍后重试");
        }
    }
}