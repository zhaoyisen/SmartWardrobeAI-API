package com.smartwardrobeai.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartwardrobeai.admin.mapper.SysUserMapper;
import com.smartwardrobeai.admin.model.dto.AdminLoginDTO;
import com.smartwardrobeai.admin.model.dto.SysUserQueryDTO;
import com.smartwardrobeai.admin.model.dto.SysUserSaveDTO;
import com.smartwardrobeai.admin.model.entity.SysUser;
import com.smartwardrobeai.admin.model.vo.AdminLoginVO;
import com.smartwardrobeai.admin.model.vo.SysUserVO;
import com.smartwardrobeai.admin.service.SysUserService;
import com.smartwardrobeai.common.BusinessException;
import com.smartwardrobeai.common.UserContext;
import com.smartwardrobeai.common.model.entity.PageResult;
import com.smartwardrobeai.utils.JwtUtil;
import com.smartwardrobeai.utils.QueryGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public PageResult<SysUserVO> pageQuery(SysUserQueryDTO queryDTO) {
        // 1. 获取分页对象
        Page<SysUser> page = queryDTO.toMpPage("id", false);

        // 2. 自动生成 QueryWrapper
        QueryWrapper<SysUser> wrapper = QueryGenerator.generate(queryDTO);

        // 3. 执行查询
        this.page(page, wrapper);

        // 4. 转换VO
        return PageResult.of(page, SysUserVO.class);
    }

    @Override
    public SysUserVO getDetail(Long id) {
        SysUser entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("用户不存在");
        }
        return BeanUtil.toBean(entity, SysUserVO.class);
    }

    @Override
    public void saveSysUser(SysUserSaveDTO saveDTO) {
        // 1. 查重：检查用户名是否已存在
        Long count = this.lambdaQuery()
                .eq(SysUser::getUsername, saveDTO.getUsername())
                .count();
        if (count > 0) {
            throw new BusinessException("用户名 [" + saveDTO.getUsername() + "] 已存在");
        }

        // 2. 校验密码
        if (saveDTO.getPassword() == null || saveDTO.getPassword().trim().isEmpty()) {
            throw new BusinessException("新增用户时密码不能为空");
        }

        // 3. 转换为实体
        SysUser entity = new SysUser();
        BeanUtils.copyProperties(saveDTO, entity);

        // 4. 密码加密
        entity.setPassword(passwordEncoder.encode(saveDTO.getPassword()));

        // 5. 默认值填充
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }

        // 6. 保存
        this.save(entity);
    }

    @Override
    public void updateSysUser(SysUserSaveDTO saveDTO) {
        if (saveDTO.getId() == null) {
            throw new IllegalArgumentException("ID不能为空");
        }

        // 1. 检查用户是否存在
        SysUser existUser = this.getById(saveDTO.getId());
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 查重：检查用户名是否已存在（排除自身）
        if (saveDTO.getUsername() != null && !saveDTO.getUsername().equals(existUser.getUsername())) {
            Long count = this.lambdaQuery()
                    .eq(SysUser::getUsername, saveDTO.getUsername())
                    .ne(SysUser::getId, saveDTO.getId())
                    .count();
            if (count > 0) {
                throw new BusinessException("用户名 [" + saveDTO.getUsername() + "] 已存在");
            }
        }

        // 3. 构建更新条件
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, saveDTO.getId());

        // 4. 如果传了密码，则加密后更新
        if (saveDTO.getPassword() != null && !saveDTO.getPassword().trim().isEmpty()) {
            updateWrapper.set(SysUser::getPassword, passwordEncoder.encode(saveDTO.getPassword()));
        }

        // 5. 更新其他字段
        if (saveDTO.getUsername() != null) {
            updateWrapper.set(SysUser::getUsername, saveDTO.getUsername());
        }
        if (saveDTO.getNickname() != null) {
            updateWrapper.set(SysUser::getNickname, saveDTO.getNickname());
        }
        if (saveDTO.getAvatar() != null) {
            updateWrapper.set(SysUser::getAvatar, saveDTO.getAvatar());
        }
        if (saveDTO.getStatus() != null) {
            updateWrapper.set(SysUser::getStatus, saveDTO.getStatus());
        }

        // 6. 执行更新
        this.update(updateWrapper);
    }

    @Override
    public void removeSysUserById(Long id) {
        // 1. 不能删除自己
        Long currentUserId = UserContext.getUserId();
        if (currentUserId != null && currentUserId.equals(id)) {
            throw new BusinessException("不能删除当前登录的管理员账号");
        }

        // 2. 检查用户是否存在
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 3. 执行删除
        this.removeById(id);
    }

    @Override
    public void removeSysUserBatchByIds(List<Long> ids) {
        // 1. 不能删除自己
        Long currentUserId = UserContext.getUserId();
        if (currentUserId != null && ids.contains(currentUserId)) {
            throw new BusinessException("不能删除当前登录的管理员账号");
        }

        // 2. 批量删除
        this.removeBatchByIds(ids);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        // 1. 不能禁用自己
        Long currentUserId = UserContext.getUserId();
        if (currentUserId != null && currentUserId.equals(id) && status == 0) {
            throw new BusinessException("不能禁用当前登录的管理员账号");
        }

        // 2. 检查用户是否存在
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 3. 更新状态
        this.update(new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .set(SysUser::getStatus, status));
    }

    @Override
    public String resetPassword(Long id) {
        // 1. 检查用户是否存在
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 生成默认密码（123456）的BCrypt加密值
        String defaultPassword = "123456";
        String encodedPassword = passwordEncoder.encode(defaultPassword);

        // 3. 更新密码
        this.update(new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .set(SysUser::getPassword, encodedPassword));

        // 4. 返回提示信息
        return "密码已重置为：" + defaultPassword;
    }
}