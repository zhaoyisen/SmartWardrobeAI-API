package com.smartwardrobeai.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartwardrobeai.common.BusinessException;
import com.smartwardrobeai.common.UserContext;
import com.smartwardrobeai.config.MinioConfig;
import com.smartwardrobeai.app.mapper.SysFileMapper;
import com.smartwardrobeai.app.model.entity.SysFile;
import com.smartwardrobeai.app.service.FileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
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
    private final RestTemplate restTemplate;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysFile uploadFromUrl(String url, String originalFileName) {
        try {
            log.info("开始从URL下载并上传文件: {}", url);

            // 1. 将字符串URL转换为URI，避免RestTemplate对查询参数进行二次编码
            // 阿里云OSS的签名URL包含查询参数（如Signature），如果被重新编码会导致签名验证失败
            URI uri = URI.create(url);

            // 2. 设置请求头，避免403 Forbidden错误
            // 某些OSS服务（如阿里云OSS）需要特定的请求头，否则会返回403
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Accept", "image/*,*/*;q=0.8");
            headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 3. 从URI下载图片数据（使用URI而不是String URL，避免URL编码问题）
            ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, byte[].class);
            if (response.getStatusCode().isError() || response.getBody() == null) {
                throw new BusinessException("从URL下载文件失败: " + url + ", 状态码: " + response.getStatusCode());
            }

            byte[] fileData = response.getBody();
            long fileSize = fileData.length;

            // 3. 计算文件 MD5 指纹
            String fileHash = DigestUtils.md5DigestAsHex(fileData);

            // 4. 检查数据库中是否已存在相同MD5的文件
            SysFile existingFile = sysFileMapper.selectOne(
                    new LambdaQueryWrapper<SysFile>().eq(SysFile::getFileHash, fileHash)
            );

            // 4. 如果存在，直接返回旧数据（实现秒传）
            if (existingFile != null) {
                log.info("检测到重复文件，触发秒传: {}", existingFile.getFileUrl());
                return existingFile;
            }

            // 5. 生成存储路径
            // 分割后的图片通常是PNG格式（透明背景）
            String suffix = ".png";
            if (originalFileName != null && originalFileName.contains(".")) {
                String originalSuffix = originalFileName.substring(originalFileName.lastIndexOf("."));
                // 如果原始文件名有后缀，使用原始后缀；否则使用.png
                if (originalSuffix.length() > 1) {
                    suffix = originalSuffix;
                }
            }
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String fileName = datePath + "/" + UUID.randomUUID().toString().replace("-", "") + suffix;

            // 6. 上传文件到 MinIO
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileData)) {
                // 根据文件后缀确定ContentType
                String contentType = "image/png"; // 默认PNG
                if (suffix.equalsIgnoreCase(".jpg") || suffix.equalsIgnoreCase(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (suffix.equalsIgnoreCase(".gif")) {
                    contentType = "image/gif";
                } else if (suffix.equalsIgnoreCase(".webp")) {
                    contentType = "image/webp";
                }

                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .object(fileName)
                                .stream(inputStream, fileSize, -1)
                                .contentType(contentType)
                                .build()
                );
            }

            // 7. 构建可访问的 URL
            String fileUrl = String.format("%s/%s/%s",
                    minioConfig.getEndpoint(),
                    minioConfig.getBucketName(),
                    fileName);

            // 8. 保存文件记录到数据库
            SysFile sysFile = new SysFile();
            sysFile.setFileName(originalFileName != null ? originalFileName : "downloaded_file" + suffix);
            sysFile.setFilePath(fileName);
            sysFile.setFileUrl(fileUrl);
            sysFile.setFileSize(fileSize);
            sysFile.setFileType(suffix);
            sysFile.setPlatform("minio");
            sysFile.setCreateBy(String.valueOf(UserContext.getUserId()));
            sysFile.setFileHash(fileHash);

            sysFileMapper.insert(sysFile);

            log.info("从URL上传文件到MinIO成功: {}", fileUrl);
            return sysFile;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("从URL上传文件到MinIO异常: url={}", url, e);
            throw new BusinessException("从URL下载并上传文件失败: " + e.getMessage());
        }
    }
}