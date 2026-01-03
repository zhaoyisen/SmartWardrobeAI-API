package com.smartwardrobeai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestService {

    @Async("taskExecutor") // 指定使用我们配置的线程池
    public void executeAsync() {
        // 如果配置成功，这里的日志也会打印出 traceId，且线程名是 async-exec-1
        log.info("====> 我是异步线程，我也要有 TraceID！");

        try { Thread.sleep(1000); } catch (InterruptedException e) {}
    }
}