package com.smartwardrobeai.app.controller;

import com.smartwardrobeai.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * App端健康检查控制器
 * 复用Spring Boot Actuator的健康检查功能，返回简单的健康状态（UP/DOWN）
 */
@RestController
@RequestMapping("api/app/health")
@Tag(name = "健康检查", description = "App端健康检查接口，返回应用健康状态")
@RequiredArgsConstructor
public class HealthController {

    private final HealthEndpoint healthEndpoint;

    /**
     * 健康检查接口
     * GET /api/app/health
     * 返回简单的健康状态（UP/DOWN）
     */
    @Operation(summary = "健康检查", description = "返回应用健康状态，UP表示健康，DOWN表示不健康")
    @GetMapping
    public Result<String> health() {
        HealthComponent health = healthEndpoint.health();
        Status status = health.getStatus();
        // 将Status转换为字符串（UP或DOWN）
        String statusString = status.getCode();
        return Result.success(statusString);
    }
}

