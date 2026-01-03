package com.smartwardrobeai.common;

import lombok.Getter;

/**
 * 自定义业务异常
 * 用于在处理业务逻辑时，抛出预期的错误信息（如：参数错误、权限不足、数据已存在等）
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final String message;

    /**
     * 根据枚举构造
     * 用法: throw new BusinessException(ResultCode.USER_NOT_EXIST);
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 自定义消息构造
     * 用法: throw new BusinessException("当前库存不足");
     * 默认使用 FAILED 的状态码
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAILED.getCode();
        this.message = message;
    }

    /**
     * 全自定义构造
     * 用法: throw new BusinessException(404, "没找到这件衣服");
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}