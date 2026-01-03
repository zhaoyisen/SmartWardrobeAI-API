package com.smartwardrobeai.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * MDC 线程上下文装饰器
 * 作用：解决 @Async 异步调用时，MDC 里的 traceId 丢失的问题
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        // 1. 【主线程】在任务提交时，捕获当前线程的 MDC 上下文
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            try {
                // 2. 【子线程】任务开始执行前，将上下文注入子线程
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }

                // 3. 执行真正的任务
                runnable.run();

            } finally {
                // 4. 【子线程】任务执行完后，必须清理，防止线程池复用导致数据污染
                MDC.clear();
            }
        };
    }
}