package com.smartwardrobeai.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartwardrobeai.app.model.entity.SysFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统附件表
 */
@Mapper
public interface SysFileMapper extends BaseMapper<SysFile> {
    // 基础的 CRUD 由 MyBatis-Plus 自动完成
}