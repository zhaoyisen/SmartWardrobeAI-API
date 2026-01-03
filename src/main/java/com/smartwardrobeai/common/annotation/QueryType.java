package com.smartwardrobeai.common.annotation;

/**
 * 通用查询类型枚举
 * 配合 @Query 注解使用
 */
public enum QueryType {

    EQ,           // = (等于)
    NE,           // != (不等于)
    GT,           // > (大于)
    GE,           // >= (大于等于)
    LT,           // < (小于)
    LE,           // <= (小于等于)

    LIKE,         // LIKE '%值%' (全模糊)
    LEFT_LIKE,    // LIKE '%值' (左模糊)
    RIGHT_LIKE,   // LIKE '值%' (右模糊)

    IN,           // IN (列表包含)
    BETWEEN,      // BETWEEN val1 AND val2 (范围查询)

    IS_NULL,      // IS NULL
    IS_NOT_NULL   // IS NOT NULL
}