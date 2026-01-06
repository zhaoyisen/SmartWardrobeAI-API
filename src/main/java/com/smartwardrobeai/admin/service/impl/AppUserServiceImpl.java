package com.smartwardrobeai.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartwardrobeai.admin.model.dto.AppUserQueryDTO;
import com.smartwardrobeai.admin.model.dto.AppUserSaveDTO;
import com.smartwardrobeai.admin.model.vo.AppUserVO;
import com.smartwardrobeai.admin.service.AppUserService;
import com.smartwardrobeai.app.mapper.ClothingMapper;
import com.smartwardrobeai.app.mapper.UserMapper;
import com.smartwardrobeai.app.model.entity.Clothing;
import com.smartwardrobeai.app.model.entity.User;
import com.smartwardrobeai.common.BusinessException;
import com.smartwardrobeai.common.model.entity.PageResult;
import com.smartwardrobeai.utils.QueryGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserServiceImpl extends ServiceImpl<UserMapper, User> implements AppUserService {


    private final ClothingMapper clothingMapper;

    @Override
    public PageResult<AppUserVO> pageQuery(AppUserQueryDTO queryDTO) {
        // 1. 获取分页对象
        Page<User> page = queryDTO.toMpPage("id", false);

        // 2. 自动生成 QueryWrapper
        QueryWrapper<User> wrapper = QueryGenerator.generate(queryDTO);

        // 3. 执行查询
        this.page(page, wrapper);

        // 4. 转换VO
        return PageResult.of(page, AppUserVO.class);
    }

    @Override
    public AppUserVO getDetail(Long id) {
        User entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("用户不存在");
        }
        return BeanUtil.toBean(entity, AppUserVO.class);
    }

    @Override
    public void updateAppUser(AppUserSaveDTO saveDTO) {
        if (saveDTO.getId() == null) {
            throw new IllegalArgumentException("ID不能为空");
        }

        // 1. 检查用户是否存在
        User existUser = this.getById(saveDTO.getId());
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 校验邮箱唯一性（如果修改了邮箱，排除自身）
        if (saveDTO.getEmail() != null && !saveDTO.getEmail().equals(existUser.getEmail())) {
            Long emailCount = this.lambdaQuery()
                    .eq(User::getEmail, saveDTO.getEmail())
                    .ne(User::getId, saveDTO.getId())
                    .count();
            if (emailCount > 0) {
                throw new BusinessException("邮箱 [" + saveDTO.getEmail() + "] 已被使用");
            }
        }

        // 3. 校验手机号唯一性（如果修改了手机号，排除自身）
        if (saveDTO.getPhone() != null && !saveDTO.getPhone().equals(existUser.getPhone())) {
            Long phoneCount = this.lambdaQuery()
                    .eq(User::getPhone, saveDTO.getPhone())
                    .ne(User::getId, saveDTO.getId())
                    .count();
            if (phoneCount > 0) {
                throw new BusinessException("手机号 [" + saveDTO.getPhone() + "] 已被使用");
            }
        }

        // 4. 转换为实体并更新（不更新密码字段）
        this.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, saveDTO.getId())
                .set(saveDTO.getUsername() != null, User::getUsername, saveDTO.getUsername())
                .set(saveDTO.getEmail() != null, User::getEmail, saveDTO.getEmail())
                .set(saveDTO.getPhone() != null, User::getPhone, saveDTO.getPhone())
                .set(saveDTO.getAvatarUrl() != null, User::getAvatarUrl, saveDTO.getAvatarUrl())
                .set(saveDTO.getHeight() != null, User::getHeight, saveDTO.getHeight())
                .set(saveDTO.getWeight() != null, User::getWeight, saveDTO.getWeight())
                .set(saveDTO.getStatus() != null, User::getStatus, saveDTO.getStatus()));
    }

    @Override
    public void removeAppUserById(Long id) {
        // 1. 检查用户是否存在
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 检查是否有关联数据（如clothing表）
        Long clothingCount = clothingMapper.selectCount(
                new LambdaQueryWrapper<Clothing>()
                        .eq(Clothing::getUserId, id)
        );
        if (clothingCount > 0) {
            throw new BusinessException("该用户存在关联的衣物数据，无法删除");
        }

        // 3. 执行删除
        this.removeById(id);
    }

    @Override
    public void removeAppUserBatchByIds(List<Long> ids) {
        // 检查每个ID是否有关联数据
        for (Long id : ids) {
            Long clothingCount = clothingMapper.selectCount(
                    new LambdaQueryWrapper<Clothing>()
                            .eq(Clothing::getUserId, id)
            );
            if (clothingCount > 0) {
                User user = this.getById(id);
                String username = user != null ? user.getUsername() : String.valueOf(id);
                throw new BusinessException("用户 [" + username + "] 存在关联的衣物数据，无法删除");
            }
        }

        // 批量删除
        this.removeBatchByIds(ids);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        // 1. 检查用户是否存在
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 更新状态
        this.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, id)
                .set(User::getStatus, status));
    }
}

