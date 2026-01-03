package com.smartwardrobeai.common.aspect;

import com.smartwardrobeai.common.BusinessException;
import com.smartwardrobeai.common.UserContext;
import com.smartwardrobeai.common.annotation.NoRepeatSubmit;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * 方法执行前“插一脚”，检查 Key 是否存在
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class NoRepeatSubmitAspect {

    private final StringRedisTemplate redisTemplate;

    @Around("@annotation(noRepeatSubmit)")
    public Object around(ProceedingJoinPoint point, NoRepeatSubmit noRepeatSubmit) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        // 1. 获取当前用户 ID (如果没有登录，可以用 IP 地址代替)
        String userId = String.valueOf(UserContext.getUserId());
        // 2. 获取请求的 URL
        String url = request.getRequestURI();

        // 3. 组合成唯一 Key: "repeat_submit:用户ID:接口URL"
        // 比如: repeat_submit:1001:/api/file/upload
        String key = "repeat_submit:" + userId + ":" + url;

        // 4. 尝试加锁 (SETNX)
        // 如果 Key 不存在，则写入并返回 true；如果 Key 已存在，返回 false
        Boolean isSuccess = redisTemplate.opsForValue().setIfAbsent(
                key,
                "1",
                noRepeatSubmit.timeout(),
                TimeUnit.MILLISECONDS
        );
        if (isSuccess != null && isSuccess) {
            // 拿到锁了，放行，执行真正的业务逻辑
            return point.proceed();
        } else {
            // 没拿到锁，说明刚才已经点过了
            throw new BusinessException("操作太频繁，请稍后再试");
        }
    }
}