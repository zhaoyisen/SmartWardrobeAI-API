package com.smartwardrobeai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartwardrobeai.common.BusinessException;
import com.smartwardrobeai.mapper.UserMapper;
import com.smartwardrobeai.model.dto.AuthResponse;
import com.smartwardrobeai.model.dto.EmailRegisterRequest;
import com.smartwardrobeai.model.dto.LoginRequest;
import com.smartwardrobeai.model.dto.SmsLoginRequest;
import com.smartwardrobeai.model.entity.User;
import com.smartwardrobeai.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 认证服务
 * 处理注册、登录及 Token 颁发逻辑
 */
@Service
@RequiredArgsConstructor // Lombok: 自动生成包含 final 字段的构造函数，实现 Spring 构造器注入
public class AuthService {

    private final UserMapper userMapper;       // MP Mapper
    private final PasswordEncoder passwordEncoder; // Security 密码加密器
    private final JwtUtil jwtUtil;             // JWT 工具类
    private final VerificationService verificationService; // 验证码服务


    /**
     * 方案1：邮箱注册
     * 必须输入：用户名、密码、邮箱、验证码
     */
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse registerByEmail(EmailRegisterRequest request) {
        // 1. 校验验证码
        verificationService.verifyCode(request.email(), "email", request.verifyCode());

        // 2. 查重 (邮箱)
        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getEmail, request.email()))) {
            throw new BusinessException("该邮箱已被注册");
        }

        // 3. 创建用户
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password())) // 设置密码
                .build();

        userMapper.insert(user);

        // 4. 自动登录
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getUsername());
    }


    /**
     * 方案2：手机短信 登录/注册 一体化接口
     * 逻辑：如果手机号已存在 -> 登录；如果不存在 -> 自动注册(无密码) -> 登录
     */
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse loginOrRegisterBySms(SmsLoginRequest request) {
        // 1. 校验短信验证码
        verificationService.verifyCode(request.phone(), "sms", request.verifyCode());

        // 2. 查询用户是否存在
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, request.phone()));

        if (user == null) {
            // --- 注册流程 ---
            // 自动生成一个用户名，例如: User_尾号4位
            String randomSuffix = request.phone().substring(7);
            String autoUsername = "User_" + randomSuffix + "_" + UUID.randomUUID().toString().substring(0, 4);

            user = User.builder()
                    .phone(request.phone())
                    .username(autoUsername)
                    .passwordHash(null) // ⚠️ 关键：密码为空
                    .build();

            userMapper.insert(user);
        }

        // --- 登录流程 (无论是新用户还是老用户，验证码通过了就发 Token) ---
        String token = jwtUtil.generateToken(user.getId(), user.getPhone());
        return new AuthResponse(token, user.getId(), user.getUsername());
    }


    /**
     * 传统密码登录 (仅支持已设置密码的用户)
     */
    public AuthResponse loginByPassword(LoginRequest request) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, request.account())
                .or()
                .eq(User::getPhone, request.account());

        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // ⚠️ 关键校验：如果用户是短信注册且没设置过密码，passwordHash 为 null
        if (user.getPasswordHash() == null) {
            throw new BusinessException("该账号未设置密码，请使用短信验证码登录");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("密码错误");
        }

        String account = user.getEmail() != null ? user.getEmail() : user.getPhone();
        String token = jwtUtil.generateToken(user.getId(), account);

        return new AuthResponse(token, user.getId(), user.getUsername());
    }


}