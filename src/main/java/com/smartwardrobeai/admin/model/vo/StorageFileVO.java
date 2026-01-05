package com.smartwardrobeai.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "【后台】存储文件信息")
public class StorageFileVO {

    @Schema(description = "文件ID")
    private Long id;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件存储路径")
    private String filePath;

    @Schema(description = "完整访问URL")
    private String fileUrl;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "格式化后的文件大小")
    private String fileSizeFormatted;

    @Schema(description = "文件扩展名")
    private String fileType;

    @Schema(description = "存储平台")
    private String platform;

    @Schema(description = "上传人标识")
    private String createBy;

    @Schema(description = "文件MD5哈希值")
    private String fileHash;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否存在于MinIO")
    private Boolean existsInMinio;

    @Schema(description = "文件是否可访问")
    private Boolean accessible;
}

