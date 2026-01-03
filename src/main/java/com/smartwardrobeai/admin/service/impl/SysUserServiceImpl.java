package com.smartwardrobeai.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartwardrobeai.admin.mapper.SysUserMapper;
import com.smartwardrobeai.admin.model.dto.AdminLoginDTO;
import com.smartwardrobeai.admin.model.entity.SysUser;
import com.smartwardrobeai.admin.model.vo.AdminLoginVO;
import com.smartwardrobeai.admin.service.SysUserService;
import com.smartwardrobeai.common.BusinessException;
import com.smartwardrobeai.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    // 依赖注入全在 Service 层完成
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtils;

    @Override
    public AdminLoginVO login(AdminLoginDTO loginDto) {
        // 1. 查数据库
        SysUser user = this.getOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, loginDto.getUsername())
        );

        // 2. 校验账号是否存在
        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }

        // 3. 校验密码 (密文比对)
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }

        // 4. 校验状态
        if (user.getStatus() == 0) {
            throw new BusinessException("该账号已被禁用");
        }

        // 5. 生成 Token (指定类型为 ADMIN)
        String token = jwtUtils.createToken(user.getId(), user.getUsername(), "ADMIN");

        AdminLoginVO vo = new AdminLoginVO();

        BeanUtils.copyProperties(user, vo);

        vo.setToken(token);

        return vo;
    }
}