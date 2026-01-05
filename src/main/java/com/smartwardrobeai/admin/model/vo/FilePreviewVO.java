package com.smartwardrobeai.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "文件预览URL信息")
public class FilePreviewVO {

    @Schema(description = "临时预览URL（带签名）")
    private String previewUrl;

    @Schema(description = "URL过期时间")
    private LocalDateTime expiresAt;
}

