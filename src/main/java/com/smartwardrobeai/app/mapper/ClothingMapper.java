package com.smartwardrobeai.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartwardrobeai.app.model.entity.Clothing;
import org.apache.ibatis.annotations.Mapper;

/**
 * 衣物表 Mapper 接口
 */
@Mapper
public interface ClothingMapper extends BaseMapper<Clothing> {
    // 暂时不需要写 SQL，复杂查询后续再加
}