package com.smartwardrobeai.common;

/**
 * 这是一个基于 ThreadLocal 的工具类，用于在当前线程中隔离存储用户信息
 */
public class UserContext {
    // 使用 ThreadLocal 存储当前线程的用户 ID
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    // 必须有清理方法，防止内存泄漏 (尤其是在线程池环境下)
    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}