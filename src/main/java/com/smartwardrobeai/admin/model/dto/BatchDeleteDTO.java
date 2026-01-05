package com.smartwardrobeai.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量删除参数")
public class BatchDeleteDTO {

    @Schema(description = "文件ID列表", required = true)
    @NotEmpty(message = "文件ID列表不能为空")
    private List<Long> ids;
}

