package com.smartwardrobeai.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "【后台】字典数据导入请求")
public class DictDataImportDTO {

    @Schema(description = "Excel文件", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile file;

    @Schema(description = "重复数据处理策略：skip(跳过) 或 update(更新)", example = "skip")
    private String duplicateStrategy = "skip";

    @Schema(description = "字典类型ID（可选，如果通过参数指定字典类型）")
    private Long dictTypeId;
}

