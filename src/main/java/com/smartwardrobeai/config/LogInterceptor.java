package com.smartwardrobeai.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class LogInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 生成唯一的 TraceID (去掉横杠让日志更紧凑)
        String traceId = UUID.randomUUID().toString().replace("-", "");

        // 2. 放入 MDC 上下文
        MDC.put(TRACE_ID, traceId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 3. 必须在请求结束时清除，防止线程池导致的内存泄漏或 ID 污染
        MDC.remove(TRACE_ID);
    }
}