package com.smartwardrobeai.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量删除结果")
public class BatchDeleteResultVO {

    @Schema(description = "成功删除的数量")
    private Integer successCount;

    @Schema(description = "失败的数量")
    private Integer failCount;

    @Schema(description = "删除失败的文件ID列表")
    private List<Long> failIds;
}

