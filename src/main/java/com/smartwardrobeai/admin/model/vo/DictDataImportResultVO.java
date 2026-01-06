package com.smartwardrobeai.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "【后台】字典数据导入结果")
public class DictDataImportResultVO {

    @Schema(description = "总记录数")
    private Integer totalCount;

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failCount;

    @Schema(description = "成功记录列表")
    private List<ImportSuccessItem> successList;

    @Schema(description = "失败记录列表")
    private List<ImportFailItem> failList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "导入成功项")
    public static class ImportSuccessItem {
        @Schema(description = "行号")
        private Integer rowNum;

        @Schema(description = "字典类型编码")
        private String dictType;

        @Schema(description = "字典标签")
        private String dictLabel;

        @Schema(description = "字典值")
        private String dictValue;

        @Schema(description = "操作类型：新增/更新")
        private String operation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "导入失败项")
    public static class ImportFailItem {
        @Schema(description = "行号")
        private Integer rowNum;

        @Schema(description = "字典类型编码")
        private String dictType;

        @Schema(description = "字典标签")
        private String dictLabel;

        @Schema(description = "字典值")
        private String dictValue;

        @Schema(description = "失败原因")
        private String errorMessage;
    }
}

