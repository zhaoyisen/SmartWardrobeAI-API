package com.smartwardrobeai.common.model.entity;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分页查询基类
 * 所有需要分页的 QueryDTO 都继承此类
 */
@Data
public class BasePageQuery {

    @Schema(description = "页码 (默认1)", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数 (默认10)", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "排序字段 (数据库列名，如 create_time)")
    private String sortField;

    @Schema(description = "是否升序 (true: asc, false: desc)")
    private Boolean isAsc = false; // 默认降序

    /**
     * 获取 MyBatis Plus 的 Page 对象
     * 方便 Service 层直接调用
     */
    public <T> Page<T> toMpPage() {
        return toMpPage(null, false);
    }


    /**
     * 转为 MyBatis Plus 的 Page 对象 (带默认排序)
     * <p>
     * 逻辑：
     * IF (前端传了 sortField) -> 使用前端的
     * ELSE IF (后端给了 defaultField) -> 使用后端的
     * ELSE -> 不排序
     * </p>
     *
     * @param defaultSortField 默认排序字段 (数据库列名，如 "id")
     * @param defaultIsAsc     默认是否升序
     * @param <T>              泛型
     * @return 组装好的 MP Page 对象
     */
    public <T> Page<T> toMpPage(String defaultSortField, boolean defaultIsAsc) {
        // 1. 创建 Page 对象
        Page<T> page = new Page<>(pageNum, pageSize);

        // 2. 决策排序字段
        // 优先取前端传的值
        String finalSortField = this.sortField;
        boolean finalIsAsc = this.isAsc != null ? this.isAsc : defaultIsAsc;

        // 如果前端没传，且后端给了默认值，则使用默认值
        if (finalSortField == null || finalSortField.trim().isEmpty()) {
            finalSortField = defaultSortField;
            // 如果用了默认字段，强制使用默认排序方向 (除非前端只传了 isAsc 没传 sortField，这种情况很少见，这里以默认逻辑为主)
            finalIsAsc = defaultIsAsc;
        }

        // 3. 执行排序注入
        if (finalSortField != null && !finalSortField.trim().isEmpty()) {
            // 🛡️ 安全检查：防止 SQL 注入
            // 只允许 字母、数字、下划线，防止前端传 "id; delete from table;"
            String cleanField = finalSortField.replaceAll("[^a-zA-Z0-9_]", "");

            // 驼峰转下划线 (可选逻辑：如果你数据库是下划线，前端传驼峰，这里可以加个转换，暂时保持原样)
            // cleanField = StrUtil.toUnderlineCase(cleanField);

            if (finalIsAsc) {
                page.addOrder(OrderItem.asc(cleanField));
            } else {
                page.addOrder(OrderItem.desc(cleanField));
            }
        }
        return page;
    }

}