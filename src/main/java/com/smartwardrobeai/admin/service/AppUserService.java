package com.smartwardrobeai.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartwardrobeai.admin.model.dto.AppUserQueryDTO;
import com.smartwardrobeai.admin.model.dto.AppUserSaveDTO;
import com.smartwardrobeai.admin.model.vo.AppUserVO;
import com.smartwardrobeai.app.model.entity.User;
import com.smartwardrobeai.common.model.entity.PageResult;

import java.util.List;

/**
 * App端用户管理Service接口
 */
public interface AppUserService extends IService<User> {

    /**
     * 分页查询App端用户列表
     */
    PageResult<AppUserVO> pageQuery(AppUserQueryDTO queryDTO);

    /**
     * 获取App端用户详情
     */
    AppUserVO getDetail(Long id);

    /**
     * 修改App端用户信息（不包含密码）
     */
    void updateAppUser(AppUserSaveDTO saveDTO);

    /**
     * 删除App端用户（会检查是否有关联数据）
     */
    void removeAppUserById(Long id);

    /**
     * 批量删除App端用户（会检查是否有关联数据）
     */
    void removeAppUserBatchByIds(List<Long> ids);

    /**
     * 修改App端用户状态
     */
    void updateStatus(Long id, Integer status);
}

