package com.smartwardrobeai.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "【后台】存储统计信息")
public class StorageStatisticsVO {

    @Schema(description = "总文件数")
    private Long totalFiles;

    @Schema(description = "总大小（字节）")
    private Long totalSize;

    @Schema(description = "格式化后的总大小（如：1.00 GB）")
    private String totalSizeFormatted;

    @Schema(description = "存储桶名称")
    private String bucketName;
}

