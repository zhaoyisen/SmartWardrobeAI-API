package com.smartwardrobeai.app.service.impl;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.smartwardrobeai.common.BusinessException;
import com.smartwardrobeai.common.UserContext;
import com.smartwardrobeai.config.QiniuConfig;
import com.smartwardrobeai.app.mapper.SysFileMapper;
import com.smartwardrobeai.app.model.entity.SysFile;
import com.smartwardrobeai.app.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

//@Service
@Slf4j
@RequiredArgsConstructor
public class QiniuStorageServiceImpl implements FileStorageService {

    private final UploadManager uploadManager;
    private final Auth auth;
    private final QiniuConfig qiniuConfig;
    private final SysFileMapper sysFileMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysFile upload(MultipartFile file) {
        try {
            // 1. 获取文件输入流
            InputStream inputStream = file.getInputStream();

            // 2. 生成文件名: date/uuid.jpg
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String key = datePath + "/" + UUID.randomUUID().toString().replace("-", "") + suffix;

            // 3. 获取上传凭证 (UpToken) - 七牛云特有机制
            // 简单上传只需传入 bucket 名字
            String upToken = auth.uploadToken(qiniuConfig.getBucket());

            // 4. 开始上传
            Response response = uploadManager.put(inputStream, key, upToken, null, null);

            // 5. 解析上传结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            log.info("七牛云上传成功, key: {}, hash: {}", putRet.key, putRet.hash);

            String fileUrl = qiniuConfig.getDomain() + "/" + putRet.key;

            SysFile sysFile = SysFile.builder()
                    .fileName(originalFilename)
                    .filePath(putRet.key)
                    .fileUrl(fileUrl)
                    .fileSize(file.getSize())
                    .fileType(suffix)
                    .platform("qiniu")
                    .createBy(UserContext.getUserId() == null ? "SYSTEM" : UserContext.getUserId().toString())
                    .build();

            sysFileMapper.insert(sysFile);
            return sysFile;
            // 6. 拼接返回 URL return qiniuConfig.getDomain() + "/" + key;

        } catch (QiniuException ex) {
            Response r = ex.response;
            log.error("七牛云上传失败: {}", r.toString());
            try {
                log.error("响应体: {}", r.bodyString());
            } catch (QiniuException e) {
                // ignore
            }
            throw new BusinessException("文件上传服务异常");
        } catch (Exception e) {
            log.error("文件上传系统错误", e);
            throw new BusinessException("文件上传失败");
        }
    }

    @Override
    public SysFile uploadFromUrl(String url, String originalFileName) {
        return null;
    }
}