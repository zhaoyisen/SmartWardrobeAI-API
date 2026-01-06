package com.smartwardrobeai.admin.model.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Excel导入字典数据实体")
public class DictDataExcelVO {

    @ExcelProperty(value = "字典类型编码", index = 0)
    @ColumnWidth(20)
    @Schema(description = "字典类型编码（必填）")
    private String dictType;

    @ExcelProperty(value = "字典标签", index = 1)
    @ColumnWidth(20)
    @Schema(description = "字典标签（必填）")
    private String dictLabel;

    @ExcelProperty(value = "字典值", index = 2)
    @ColumnWidth(20)
    @Schema(description = "字典值（必填）")
    private String dictValue;

    @ExcelProperty(value = "AI提示词", index = 3)
    @ColumnWidth(30)
    @Schema(description = "AI提示词补充（可选）")
    private String promptText;

    @ExcelProperty(value = "备注", index = 4)
    @ColumnWidth(20)
    @Schema(description = "备注说明（可选）")
    private String remark;

    @ExcelProperty(value = "排序值", index = 5)
    @ColumnWidth(15)
    @Schema(description = "排序值（可选，默认为0）")
    private Integer sort;

    @ExcelProperty(value = "状态", index = 6)
    @ColumnWidth(15)
    @Schema(description = "状态：1启用 0禁用（可选，默认为1）")
    private Integer status;
}

