package com.smartwardrobeai.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 核心配置类
 * 用于注册插件 (拦截器)
 */
@Configuration
@MapperScan("com.smartwardrobeai.mapper")
public class MybatisPlusConfig {

    /**
     * 注册 MyBatis-Plus 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 添加分页插件
        // DbType.MYSQL: 指定数据库类型，有助于 MP 优化 SQL 分页方言
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        // 2. (可选) 添加乐观锁插件
        // 如果实体类中有 @Version 字段，更新时会自动检查版本
        // interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 3. (可选) 防止全表更新插件
        // 禁止执行不带 WHERE 条件的 UPDATE/DELETE 语句，防止删库跑路
        // interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }
}