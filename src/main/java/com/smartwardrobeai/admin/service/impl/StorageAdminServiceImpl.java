package com.smartwardrobeai.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartwardrobeai.admin.model.dto.BatchDeleteDTO;
import com.smartwardrobeai.admin.model.dto.StorageFileQueryDTO;
import com.smartwardrobeai.admin.model.vo.BatchDeleteResultVO;
import com.smartwardrobeai.admin.model.vo.FilePreviewVO;
import com.smartwardrobeai.admin.model.vo.StorageFileVO;
import com.smartwardrobeai.admin.model.vo.StorageStatisticsVO;
import com.smartwardrobeai.admin.service.StorageAdminService;
import com.smartwardrobeai.app.mapper.SysFileMapper;
import com.smartwardrobeai.app.model.entity.SysFile;
import com.smartwardrobeai.common.BusinessException;
import com.smartwardrobeai.common.model.entity.PageResult;
import com.smartwardrobeai.config.MinioConfig;
import com.smartwardrobeai.utils.QueryGenerator;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageAdminServiceImpl implements StorageAdminService {

    private final SysFileMapper sysFileMapper;
    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @Override
    public PageResult<StorageFileVO> pageQuery(StorageFileQueryDTO queryDTO) {
        String viewMode = queryDTO.getViewMode() != null ? queryDTO.getViewMode() : "db";

        if ("minio".equals(viewMode)) {
            return pageQueryFromMinio(queryDTO);
        } else if ("both".equals(viewMode)) {
            return pageQueryBoth(queryDTO);
        } else {
            return pageQueryFromDb(queryDTO);
        }
    }

    /**
     * 从数据库查询
     */
    private PageResult<StorageFileVO> pageQueryFromDb(StorageFileQueryDTO queryDTO) {
        Page<SysFile> page = queryDTO.toMpPage("create_time", false);
        QueryWrapper<SysFile> wrapper = QueryGenerator.generate(queryDTO);
        sysFileMapper.selectPage(page, wrapper);

        return PageResult.of(page, (sysFile) -> {
            StorageFileVO vo = convertToVO(sysFile);
            // 检查MinIO文件是否存在
            checkMinioFileExists(vo, sysFile.getFilePath());
            return vo;
        });
    }

    /**
     * 从MinIO查询
     */
    private PageResult<StorageFileVO> pageQueryFromMinio(StorageFileQueryDTO queryDTO) {
        try {
            List<StorageFileVO> allFiles = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                if (!item.isDir()) {
                    StorageFileVO vo = convertMinioItemToVO(item);
                    allFiles.add(vo);
                }
            }

            // 手动分页
            int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
            int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, allFiles.size());

            List<StorageFileVO> pageList = start < allFiles.size() 
                    ? allFiles.subList(start, end) 
                    : new ArrayList<>();

            PageResult<StorageFileVO> result = new PageResult<>();
            result.setRecords(pageList);
            result.setTotal((long) allFiles.size());
            result.setPages((long) Math.ceil((double) allFiles.size() / pageSize));
            result.setCurrent((long) pageNum);
            result.setSize((long) pageSize);

            return result;
        } catch (Exception e) {
            log.error("查询MinIO文件列表失败", e);
            throw new BusinessException("查询MinIO文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 合并数据库和MinIO查询
     */
    private PageResult<StorageFileVO> pageQueryBoth(StorageFileQueryDTO queryDTO) {
        // 先查询数据库
        PageResult<StorageFileVO> dbResult = pageQueryFromDb(queryDTO);
        
        // 查询MinIO文件
        try {
            List<StorageFileVO> minioFiles = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                if (!item.isDir()) {
                    StorageFileVO vo = convertMinioItemToVO(item);
                    // 检查是否在数据库中存在
                    SysFile dbFile = sysFileMapper.selectOne(
                            new QueryWrapper<SysFile>().eq("file_path", item.objectName())
                    );
                    if (dbFile != null) {
                        vo.setId(dbFile.getId());
                        vo.setFileName(dbFile.getFileName());
                        vo.setCreateBy(dbFile.getCreateBy());
                        vo.setCreateTime(dbFile.getCreateTime());
                        vo.setUpdateTime(dbFile.getUpdateTime());
                    }
                    minioFiles.add(vo);
                }
            }

            // 合并结果（去重，优先使用数据库记录）
            // 这里简化处理，返回数据库查询结果，但标记MinIO文件状态
            for (StorageFileVO vo : dbResult.getRecords()) {
                checkMinioFileExists(vo, vo.getFilePath());
            }

            return dbResult;
        } catch (Exception e) {
            log.error("查询MinIO文件列表失败", e);
            // MinIO查询失败时，只返回数据库结果
            return dbResult;
        }
    }

    @Override
    public StorageFileVO getDetail(Long id) {
        SysFile sysFile = sysFileMapper.selectById(id);
        if (sysFile == null) {
            throw new BusinessException("文件不存在");
        }

        StorageFileVO vo = convertToVO(sysFile);
        checkMinioFileExists(vo, sysFile.getFilePath());
        return vo;
    }

    @Override
    public StorageStatisticsVO getStatistics() {
        StorageStatisticsVO statistics = new StorageStatisticsVO();
        
        // 查询数据库统计
        List<SysFile> allFiles = sysFileMapper.selectList(
                new QueryWrapper<SysFile>().eq("platform", "minio")
        );
        
        long totalFiles = allFiles.size();
        long totalSize = allFiles.stream()
                .mapToLong(file -> file.getFileSize() != null ? file.getFileSize() : 0)
                .sum();

        statistics.setTotalFiles(totalFiles);
        statistics.setTotalSize(totalSize);
        statistics.setTotalSizeFormatted(formatFileSize(totalSize));
        statistics.setBucketName(minioConfig.getBucketName());

        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long id) {
        SysFile sysFile = sysFileMapper.selectById(id);
        if (sysFile == null) {
            throw new BusinessException("文件不存在");
        }

        String filePath = sysFile.getFilePath();
        
        // 先删除MinIO文件
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(filePath)
                            .build()
            );
            log.info("MinIO文件删除成功: {}", filePath);
        } catch (Exception e) {
            log.error("MinIO文件删除失败: {}", filePath, e);
            // MinIO删除失败时，记录日志但继续删除数据库记录
        }

        // 删除数据库记录
        sysFileMapper.deleteById(id);
        log.info("数据库文件记录删除成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchDeleteResultVO batchDelete(BatchDeleteDTO batchDeleteDTO) {
        List<Long> ids = batchDeleteDTO.getIds();
        BatchDeleteResultVO result = new BatchDeleteResultVO();
        List<Long> failIds = new ArrayList<>();
        int successCount = 0;

        for (Long id : ids) {
            try {
                deleteFile(id);
                successCount++;
            } catch (Exception e) {
                log.error("删除文件失败: id={}", id, e);
                failIds.add(id);
            }
        }

        result.setSuccessCount(successCount);
        result.setFailCount(failIds.size());
        result.setFailIds(failIds);

        return result;
    }

    @Override
    public InputStream downloadFile(Long id) {
        SysFile sysFile = sysFileMapper.selectById(id);
        if (sysFile == null) {
            throw new BusinessException("文件不存在");
        }

        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(sysFile.getFilePath())
                            .build()
            );
        } catch (Exception e) {
            log.error("下载文件失败: id={}", id, e);
            throw new BusinessException("下载文件失败: " + e.getMessage());
        }
    }

    @Override
    public FilePreviewVO getPreviewUrl(Long id, Integer expiresIn) {
        SysFile sysFile = sysFileMapper.selectById(id);
        if (sysFile == null) {
            throw new BusinessException("文件不存在");
        }

        try {
            int expiry = expiresIn != null ? expiresIn : 3600;
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(sysFile.getFilePath())
                            .expiry(expiry, TimeUnit.SECONDS)
                            .build()
            );

            FilePreviewVO vo = new FilePreviewVO();
            vo.setPreviewUrl(url);
            vo.setExpiresAt(LocalDateTime.now().plusSeconds(expiry));

            return vo;
        } catch (Exception e) {
            log.error("生成预览URL失败: id={}", id, e);
            throw new BusinessException("生成预览URL失败: " + e.getMessage());
        }
    }

    /**
     * 转换SysFile为StorageFileVO
     */
    private StorageFileVO convertToVO(SysFile sysFile) {
        StorageFileVO vo = new StorageFileVO();
        BeanUtil.copyProperties(sysFile, vo);
        vo.setFileSizeFormatted(formatFileSize(sysFile.getFileSize() != null ? sysFile.getFileSize() : 0));
        return vo;
    }

    /**
     * 转换MinIO Item为StorageFileVO
     */
    private StorageFileVO convertMinioItemToVO(Item item) {
        StorageFileVO vo = new StorageFileVO();
        vo.setFilePath(item.objectName());
        vo.setFileSize(item.size());
        vo.setFileSizeFormatted(formatFileSize(item.size()));
        
        // 从路径提取文件名和类型
        String objectName = item.objectName();
        int lastSlash = objectName.lastIndexOf('/');
        String fileName = lastSlash >= 0 ? objectName.substring(lastSlash + 1) : objectName;
        vo.setFileName(fileName);
        
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot >= 0) {
            vo.setFileType(fileName.substring(lastDot));
        }
        
        vo.setPlatform("minio");
        vo.setFileUrl(String.format("%s/%s/%s", 
                minioConfig.getEndpoint(), 
                minioConfig.getBucketName(), 
                objectName));
        vo.setExistsInMinio(true);
        vo.setAccessible(true);
        
        // 设置时间
        if (item.lastModified() != null) {
            vo.setUpdateTime(LocalDateTime.ofInstant(
                    item.lastModified().toInstant(), 
                    ZoneId.systemDefault()
            ));
        }
        
        return vo;
    }

    /**
     * 检查MinIO文件是否存在
     */
    private void checkMinioFileExists(StorageFileVO vo, String filePath) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(filePath)
                            .build()
            );
            vo.setExistsInMinio(true);
            vo.setAccessible(true);
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                vo.setExistsInMinio(false);
                vo.setAccessible(false);
            } else {
                vo.setExistsInMinio(null);
                vo.setAccessible(false);
            }
        } catch (Exception e) {
            log.warn("检查MinIO文件状态失败: {}", filePath, e);
            vo.setExistsInMinio(null);
            vo.setAccessible(false);
        }
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}

