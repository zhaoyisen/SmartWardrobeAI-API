package com.smartwardrobeai.common.model.entity;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用分页返回对象
 *
 * @param <T> 数据列表的类型 (通常是 VO)
 */
@Data
@Schema(description = "分页结果集")
public class PageResult<T> {

    @Schema(description = "当前页数据列表")
    private List<T> records;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "总页数")
    private Long pages;

    @Schema(description = "当前页码")
    private Long current;

    @Schema(description = "每页条数")
    private Long size;

    // 空构造
    public PageResult() {
    }

    /**
     * 构造函数：直接从 MyBatis Plus 的 IPage 转换
     * 适用于 Entity 和 VO 相同的情况
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    /**
     * 构造函数：从 Entity Page 转换为 VO PageResult (最常用)
     * * @param page      MyBatis Plus 查询出来的 Entity Page
     *
     * @param converter 转换函数 (例如: UserVO::new)
     */
    public static <E, V> PageResult<V> of(IPage<E> page, Function<E, V> converter) {
        PageResult<V> result = new PageResult<>();

        // 1. 转换 List<Entity> -> List<VO>
        List<E> records = page.getRecords();
        if (records == null || records.isEmpty()) {
            result.setRecords(Collections.emptyList());
        } else {
            List<V> voList = records.stream().map(converter).collect(Collectors.toList());
            result.setRecords(voList);
        }

        // 2. 拷贝分页参数
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());

        return result;
    }


    public static <E, V> PageResult<V> of(IPage<E> page, Class<V> targetClass) {
        PageResult<V> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());

        // 偷懒核心：利用 BeanUtils 自动拷贝 List
        // 注意：这里需要你引入 Hutool 的 BeanUtil，或者自己写一个循环 copy
        // 推荐使用 Hutool: BeanUtil.copyToList(page.getRecords(), targetClass)
        if (page.getRecords() != null) {
            // 如果没有 Hutool，用 Spring BeanUtils 也可以，稍微麻烦点，这里假设用 Hutool
            result.setRecords(cn.hutool.core.bean.BeanUtil.copyToList(page.getRecords(), targetClass));
        }
        return result;
    }

}