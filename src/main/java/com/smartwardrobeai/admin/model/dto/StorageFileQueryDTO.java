package com.smartwardrobeai.admin.model.dto;

import com.smartwardrobeai.common.annotation.Query;
import com.smartwardrobeai.common.annotation.QueryType;
import com.smartwardrobeai.common.model.entity.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "【后台】存储文件分页查询参数")
public class StorageFileQueryDTO extends BasePageQuery {

    @Schema(description = "文件名（模糊查询）")
    @Query(type = QueryType.LIKE)
    private String fileName;

    @Schema(description = "文件类型（精确匹配）", example = ".jpg")
    @Query(type = QueryType.EQ)
    private String fileType;

    @Schema(description = "存储平台", example = "minio")
    @Query(type = QueryType.EQ)
    private String platform;

    @Schema(description = "上传人标识")
    @Query(type = QueryType.EQ, column = "create_by")
    private String createBy;

    @Schema(description = "开始时间（格式：yyyy-MM-dd HH:mm:ss）")
    @Query(type = QueryType.GE, column = "create_time")
    private String startTime;

    @Schema(description = "结束时间（格式：yyyy-MM-dd HH:mm:ss）")
    @Query(type = QueryType.LE, column = "create_time")
    private String endTime;

    @Schema(description = "查看模式：db（数据库记录）、minio（MinIO实时文件）、both（两者都显示）", example = "db")
    private String viewMode = "db";
}

