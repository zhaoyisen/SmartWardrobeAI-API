package com.smartwardrobeai.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartwardrobeai.common.annotation.Query;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

/**
 * 动态查询生成器
 * <p>
 * 作用：解析 DTO 上的 @Query 注解，自动生成 MyBatis-Plus 的 QueryWrapper
 * </p>
 */
@Slf4j
public class QueryGenerator {

    /**
     * 生成 QueryWrapper
     *
     * @param queryDTO 查询参数对象 (DTO)
     * @param <T>      Entity 类型
     * @return 组装好的 QueryWrapper
     */
    public static <T> QueryWrapper<T> generate(Object queryDTO) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        if (queryDTO == null) {
            return wrapper;
        }

        // 1. 获取所有字段 (利用 Hutool 反射工具，包含父类字段)
        Field[] fields = ReflectUtil.getFields(queryDTO.getClass());

        for (Field field : fields) {
            try {
                // 2. 获取字段上的 @Query 注解
                Query query = field.getAnnotation(Query.class);
                if (query == null) {
                    continue; // 没有注解的字段跳过
                }

                // 3. 获取字段值
                field.setAccessible(true);
                Object val = field.get(queryDTO);

                // 4. 空值校验：如果值为 null 或 空字符串，不拼 SQL
                if (val == null) {
                    continue;
                }
                if (val instanceof String && StrUtil.isBlank((String) val)) {
                    continue;
                }
                // 如果是空集合，也跳过
                if (val instanceof Collection && CollUtil.isEmpty((Collection<?>) val)) {
                    continue;
                }

                // 5. 确定数据库列名
                // 如果注解里没写 column，就自动把 fieldName (驼峰) 转为 under_line (下划线)
                String column = StrUtil.isBlank(query.column())
                        ? StrUtil.toUnderlineCase(field.getName())
                        : query.column();

                // 6. 核心：根据 type 拼接不同类型的 SQL
                switch (query.type()) {
                    case EQ:
                        wrapper.eq(column, val);
                        break;
                    case NE:
                        wrapper.ne(column, val);
                        break;
                    case GT:
                        wrapper.gt(column, val);
                        break;
                    case GE:
                        wrapper.ge(column, val);
                        break;
                    case LT:
                        wrapper.lt(column, val);
                        break;
                    case LE:
                        wrapper.le(column, val);
                        break;
                    case LIKE:
                        // 处理多字段模糊匹配 (例如：keyword 同时搜 name 和 code)
                        if (StrUtil.isNotBlank(query.blurry())) {
                            String[] blurryColumns = query.blurry().split(",");
                            // 生成 SQL: AND (col1 LIKE %val% OR col2 LIKE %val%)
                            wrapper.and(w -> {
                                for (String col : blurryColumns) {
                                    w.like(col, val).or();
                                }
                            });
                        } else {
                            wrapper.like(column, val);
                        }
                        break;
                    case LEFT_LIKE:
                        wrapper.likeLeft(column, val);
                        break;
                    case RIGHT_LIKE:
                        wrapper.likeRight(column, val);
                        break;
                    case IN:
                        if (val instanceof Collection) {
                            wrapper.in(column, (Collection<?>) val);
                        }
                        break;
                    case BETWEEN:
                        // 约定：前端传 List，且必须有 2 个元素 [start, end]
                        if (val instanceof List) {
                            List<?> betweenList = (List<?>) val;
                            if (betweenList.size() == 2) {
                                wrapper.between(column, betweenList.get(0), betweenList.get(1));
                            }
                        }
                        break;
                    case IS_NULL:
                        wrapper.isNull(column);
                        break;
                    case IS_NOT_NULL:
                        wrapper.isNotNull(column);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                log.error("动态查询组装失败: field={}", field.getName(), e);
            }
        }
        return wrapper;
    }
}