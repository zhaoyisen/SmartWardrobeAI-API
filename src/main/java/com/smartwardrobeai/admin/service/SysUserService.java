package com.smartwardrobeai.admin.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.smartwardrobeai.admin.model.dto.AdminLoginDTO;
import com.smartwardrobeai.admin.model.dto.SysUserQueryDTO;
import com.smartwardrobeai.admin.model.dto.SysUserSaveDTO;
import com.smartwardrobeai.admin.model.entity.SysUser;
import com.smartwardrobeai.admin.model.vo.AdminLoginVO;
import com.smartwardrobeai.admin.model.vo.SysUserVO;
import com.smartwardrobeai.common.model.entity.PageResult;

import java.util.List;

public interface SysUserService extends IService<SysUser> {

    /**
     * 后台管理员登录
     * @param loginDto 登录参数
     * @return 登录成功后的 VO (含 Token)
     */
    AdminLoginVO login(AdminLoginDTO loginDto);

    /**
     * 分页查询管理端用户列表
     */
    PageResult<SysUserVO> pageQuery(SysUserQueryDTO queryDTO);

    /**
     * 获取管理端用户详情
     */
    SysUserVO getDetail(Long id);

    /**
     * 新增管理端用户
     */
    void saveSysUser(SysUserSaveDTO saveDTO);

    /**
     * 修改管理端用户
     */
    void updateSysUser(SysUserSaveDTO saveDTO);

    /**
     * 删除管理端用户（不能删除自己）
     */
    void removeSysUserById(Long id);

    /**
     * 批量删除管理端用户（不能删除自己）
     */
    void removeSysUserBatchByIds(List<Long> ids);

    /**
     * 修改管理端用户状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 重置管理端用户密码（重置为123456）
     */
    String resetPassword(Long id);
}