package com.smartwardrobeai.controller;

import com.smartwardrobeai.common.Result;
import com.smartwardrobeai.common.annotation.NoRepeatSubmit;
import com.smartwardrobeai.common.validation.NotEmptyFile;
import com.smartwardrobeai.model.entity.SysFile;
import com.smartwardrobeai.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@Tag(name = "文件服务")
@RequiredArgsConstructor
@Validated //开启参数校验
public class FileController {

    private final FileStorageService fileStorageService;

    @Operation(summary = "上传图片",
            description = "仅支持 jpg/png 格式",
            //明确告诉 Swagger 这是一个 multipart/form-data 请求
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
    )
    @NoRepeatSubmit(timeout = 3000)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<SysFile> upload(@RequestParam("file") @NotEmptyFile(message = "图片文件不能为空") MultipartFile file) {
        SysFile sysFile = fileStorageService.upload(file);
        return Result.success(sysFile);
    }
}