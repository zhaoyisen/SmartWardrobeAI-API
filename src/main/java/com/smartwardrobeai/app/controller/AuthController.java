package com.smartwardrobeai.app.controller;

import com.smartwardrobeai.app.model.dto.*;
import com.smartwardrobeai.common.Result;
import com.smartwardrobeai.app.service.AuthService;
import com.smartwardrobeai.app.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("app/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证模块", description = "包含注册、登录、验证码发送")
public class AuthController {

    private final AuthService authService;
    private final VerificationService verificationService;

    /**
     * 发送验证码接口
     * POST /api/auth/send-code
     * body: { "target": "13800000000", "type": "sms" }
     */
    @Operation(summary = "发送验证码", description = "支持发送短信验证码或邮箱验证码，有效期5分钟")
    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestBody @Valid SendCodeRequest request) {
        verificationService.sendCode(request.target(), request.type());
        return Result.success("验证码发送成功");
    }

    /**
     * 方案1：邮箱注册 (需验证码)
     */
    @Operation(summary = "邮箱注册", description = "需要用户名、密码、邮箱及验证码")
    @PostMapping("/register/email")
    public Result<AuthResponse> registerByEmail(@RequestBody @Valid EmailRegisterRequest request) {
        return Result.success(authService.registerByEmail(request));
    }

    /**
     * 方案2：手机号验证码登录/注册 (一体化)
     */
    @Operation(summary = "手机号登录/注册", description = "如果手机号不存在则自动注册（无密码），存在则直接登录")
    @PostMapping("/login/sms")
    public Result<AuthResponse> loginBySms(@RequestBody @Valid SmsLoginRequest request) {
        return Result.success(authService.loginOrRegisterBySms(request));
    }

    /**
     * 传统密码登录 (支持手机号或邮箱，但前提是有密码)
     */
    @Operation(summary = "密码登录", description = "支持邮箱或手机号+密码登录")
    @PostMapping("/login/password")
    public Result<AuthResponse> loginByPassword(@RequestBody @Valid LoginRequest request) {
        return Result.success(authService.loginByPassword(request));
    }
}