package com.smartwardrobeai.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 通用查询注解
 * 用于在 DTO 字段上声明查询规则，配合 QueryGenerator 自动生成 SQL
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    /**
     * 数据库中的字段名 (列名)
     * <p>
     * 1. 默认留空：系统会自动将 Java 驼峰属性名转为下划线 (userId -> user_id)。
     * 2. 指定值：例如 "create_time"。
     * </p>
     */
    String column() default "";

    /**
     * 查询方式
     * 默认为精确匹配 (EQ)
     */
    QueryType type() default QueryType.EQ;

    /**
     * 多字段模糊搜索 (仅当 type=LIKE 时有效)
     * <p>
     * 用法：blurry = "label,model_key"
     * 生成 SQL：AND (label LIKE ? OR model_key LIKE ?)
     * </p>
     */
    String blurry() default "";
}