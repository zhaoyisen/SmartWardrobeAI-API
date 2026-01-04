package com.smartwardrobeai.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartwardrobeai.admin.model.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {
    // 基础 CRUD 由 MP 自动完成，暂无需手写 SQL
}

