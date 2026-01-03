package com.smartwardrobeai.controller;

import com.smartwardrobeai.common.Result;
import com.smartwardrobeai.model.dto.*;
import com.smartwardrobeai.service.AuthService;
import com.smartwardrobeai.service.TestService;
import com.smartwardrobeai.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Tag(name = "test", description = "test")
@Slf4j
public class TestController {

    private final TestService testService;

    @GetMapping("/test/async")
    public Result<String> testAsync() {
        log.info("====> 主线程开始");
        testService.executeAsync();
        log.info("====> 主线程结束");
        return Result.success("OK");
    }
}