package com.smartwardrobeai.handler;

import com.smartwardrobeai.common.BusinessException;
import com.smartwardrobeai.common.Result;
import com.smartwardrobeai.common.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j // 引入日志
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. 专门处理业务异常
     * 特点：不需要打印堆栈日志（因为这是预期的业务逻辑），只记录 INFO 级别日志即可
     */
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException e) {
        log.error("业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 2. 处理 DTO 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> ((FieldError) error).getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.error(ResultCode.VALIDATE_FAILED.getCode(), message);
    }


    /**
     * 处理方法参数校验异常 (@RequestParam, @PathVariable 等校验失败)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        // 拼接错误信息 (可能有多个参数校验失败)
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("参数校验错误");
        log.warn("参数校验失败: {}", message);
        return Result.error(message);
    }


    /**
     * 3. 兜底处理系统异常 (如 NullPointerException, SQLSyntaxErrorException)
     * 特点：这是一个 Bug，必须打印堆栈 Error 日志，且返回给前端的信息尽量模糊，避免暴露系统细节
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统未知异常", e); // 打印完整堆栈
        return Result.error(ResultCode.FAILED.getCode(), "系统繁忙，请稍后再试");
    }
}