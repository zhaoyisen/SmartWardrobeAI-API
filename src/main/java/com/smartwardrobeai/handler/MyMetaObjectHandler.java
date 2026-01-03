package com.smartwardrobeai.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自动填充基础字段
 */
@Component
public class MyMetaObjectHandler
        implements MetaObjectHandler {

    public MyMetaObjectHandler() {
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        // 对应 Entity 中的 field 名称，不是数据库字段名
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}