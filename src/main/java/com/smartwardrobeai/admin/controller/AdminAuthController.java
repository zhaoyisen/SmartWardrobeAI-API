package com.smartwardrobeai.admin.controller;

import com.smartwardrobeai.admin.model.dto.AdminLoginDTO;
import com.smartwardrobeai.admin.model.vo.AdminLoginVO;
import com.smartwardrobeai.admin.service.SysUserService;
import com.smartwardrobeai.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth") // 🔥 Admin 专属路径
@RequiredArgsConstructor
@Tag(name = "管理端认证接口", description = "后台管理登录 注销")
public class AdminAuthController {

    private final SysUserService sysUserService;

    public static void main(String[] args) {
        // 手动 new 一个编码器（不需要启动 Spring 容器）
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 生成 123456 的密文
        String encoded = encoder.encode("123456");

        System.out.println("====== 复制下面的字符串 ======");
        System.out.println(encoded);
        System.out.println("===========================");

        // 验证一下是否匹配 (应该输出 true)
        System.out.println("验证结果: " + encoder.matches("123456", encoded));
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result<AdminLoginVO> login(@RequestBody AdminLoginDTO dto) {
        return Result.success(sysUserService.login(dto));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<AdminLoginVO> logout(HttpServletRequest request) {




        return Result.success(null);
    }


}