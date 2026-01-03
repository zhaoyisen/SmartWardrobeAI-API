package com.smartwardrobeai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartwardrobeai.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问接口
 * <p>
 * 继承 BaseMapper<User> 后，自动拥有以下方法：
 * - insert(User user)
 * - deleteById(Long id)
 * - updateById(User user)
 * - selectById(Long id)
 * - selectOne(Wrapper wrapper)
 * - selectList(Wrapper wrapper)
 * ... 以及更多
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 如果后续有复杂的联表查询 (Join)，可以在这里定义方法并在 xml 中编写 SQL
    // 目前阶段使用 MP 内置方法即可满足需求
}