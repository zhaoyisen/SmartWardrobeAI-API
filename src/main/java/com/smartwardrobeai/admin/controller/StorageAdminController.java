package com.smartwardrobeai.admin.controller;

import com.smartwardrobeai.admin.model.dto.BatchDeleteDTO;
import com.smartwardrobeai.admin.model.dto.StorageFileQueryDTO;
import com.smartwardrobeai.admin.model.vo.BatchDeleteResultVO;
import com.smartwardrobeai.admin.model.vo.FilePreviewVO;
import com.smartwardrobeai.admin.model.vo.StorageFileVO;
import com.smartwardrobeai.admin.model.vo.StorageStatisticsVO;
import com.smartwardrobeai.admin.service.StorageAdminService;
import com.smartwardrobeai.app.model.entity.SysFile;
import com.smartwardrobeai.app.service.FileStorageService;
import com.smartwardrobeai.common.Result;
import com.smartwardrobeai.common.annotation.NoRepeatSubmit;
import com.smartwardrobeai.common.model.entity.PageResult;
import com.smartwardrobeai.common.validation.NotEmptyFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/admin/storage")
@Tag(name = "【后台】存储管理")
@RequiredArgsConstructor
public class StorageAdminController {

    private final StorageAdminService storageAdminService;

    @GetMapping("/files/page")
    @Operation(summary = "分页查询文件列表", description = "支持多条件筛选，支持查看模式：db（数据库记录）、minio（MinIO实时文件）、both（两者都显示）")
    public Result<PageResult<StorageFileVO>> page(StorageFileQueryDTO queryDTO) {
        return Result.success(storageAdminService.pageQuery(queryDTO));
    }

    private final FileStorageService fileStorageService;

    @Operation(summary = "上传图片",
            description = "仅支持 jpg/png 格式",
            //明确告诉 Swagger 这是一个 multipart/form-data 请求
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
    )
    @NoRepeatSubmit(timeout = 3000)
    @PostMapping(value = "files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<SysFile> upload(@RequestParam("file") @NotEmptyFile(message = "图片文件不能为空") MultipartFile file) {
        SysFile sysFile = fileStorageService.upload(file);
        return Result.success(sysFile);
    }


    @GetMapping("/files/{id}")
    @Operation(summary = "获取文件详情")
    public Result<StorageFileVO> getDetail(@PathVariable Long id) {
        return Result.success(storageAdminService.getDetail(id));
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取存储桶统计信息", description = "获取总文件数、总大小等统计信息")
    public Result<StorageStatisticsVO> getStatistics() {
        return Result.success(storageAdminService.getStatistics());
    }

    @DeleteMapping("/files/{id}")
    @Operation(summary = "删除单个文件", description = "同时删除数据库记录和MinIO文件")
    public Result<Void> delete(@PathVariable Long id) {
        storageAdminService.deleteFile(id);
        return Result.success(null, "删除成功");
    }

    @DeleteMapping("/files/batch")
    @Operation(summary = "批量删除文件", description = "批量删除文件，同时删除数据库记录和MinIO文件")
    public Result<BatchDeleteResultVO> batchDelete(@RequestBody @Valid BatchDeleteDTO batchDeleteDTO) {
        BatchDeleteResultVO result = storageAdminService.batchDelete(batchDeleteDTO);
        String message = String.format("批量删除完成，成功：%d个，失败：%d个", 
                result.getSuccessCount(), result.getFailCount());
        return Result.success(result, message);
    }

    @GetMapping("/files/{id}/download")
    @Operation(summary = "下载文件", description = "下载指定文件，返回文件流")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        try {
            StorageFileVO fileVO = storageAdminService.getDetail(id);
            if (fileVO == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            InputStream inputStream = storageAdminService.downloadFile(id);
            
            // 设置响应头
            String fileName = fileVO.getFileName() != null ? fileVO.getFileName() : "file";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            
            if (fileVO.getFileSize() != null) {
                response.setContentLengthLong(fileVO.getFileSize());
            }

            // 写入响应流
            StreamUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.error("下载文件失败: id={}", id, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/files/{id}/preview")
    @Operation(summary = "获取文件预览URL", description = "获取文件的临时预览URL（带过期时间）")
    public Result<FilePreviewVO> getPreview(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "3600") Integer expiresIn) {
        return Result.success(storageAdminService.getPreviewUrl(id, expiresIn));
    }
}

