package com.smartwardrobeai.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartwardrobeai.admin.model.entity.SysDictType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {
    // 基础 CRUD 由 MP 自动完成，暂无需手写 SQL
}

