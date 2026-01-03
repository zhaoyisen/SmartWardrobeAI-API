package com.smartwardrobeai.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartwardrobeai.admin.model.dto.AdminLoginDTO;
import com.smartwardrobeai.admin.model.entity.SysUser;
import com.smartwardrobeai.admin.model.vo.AdminLoginVO;

public interface SysUserService extends IService<SysUser> {

    /**
     * 后台管理员登录
     * @param loginDto 登录参数
     * @return 登录成功后的 VO (含 Token)
     */
    AdminLoginVO login(AdminLoginDTO loginDto);
}