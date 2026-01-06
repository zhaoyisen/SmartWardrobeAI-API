package com.smartwardrobeai.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID = "traceId";
    private static final String START_TIME = "startTime";
    
    // 敏感字段列表，需要脱敏处理
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "pwd", "passwd", "token", "authorization", 
            "secret", "secretKey", "apiKey", "accessToken", "refreshToken"
    );

    // 使用 ThreadLocal 存储请求开始时间
    private static final ThreadLocal<Long> START_TIME_HOLDER = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 生成唯一的 TraceID (去掉横杠让日志更紧凑)
        String traceId = UUID.randomUUID().toString().replace("-", "");

        // 2. 放入 MDC 上下文
        MDC.put(TRACE_ID, traceId);

        // 3. 记录请求开始时间
        long startTime = System.currentTimeMillis();
        START_TIME_HOLDER.set(startTime);

        // 4. 记录请求信息
        logRequest(request, handler);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 1. 记录响应信息
        Long startTime = START_TIME_HOLDER.get();
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            logResponse(request, response, handler, duration, ex);
            START_TIME_HOLDER.remove();
        }

        // 2. 必须在请求结束时清除，防止线程池导致的内存泄漏或 ID 污染
        MDC.remove(TRACE_ID);
    }

    /**
     * 记录请求信息
     */
    private void logRequest(HttpServletRequest request, Object handler) {
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            String clientIp = getClientIp(request);

            // 获取方法信息
            String handlerInfo = getHandlerInfo(handler);

            // 获取请求参数（Query 参数）
            Map<String, String> params = getRequestParams(request);

            // 构建日志消息
            StringBuilder logMsg = new StringBuilder();
            logMsg.append("请求开始: [").append(method).append("] ").append(uri);
            
            if (queryString != null && !queryString.isEmpty()) {
                logMsg.append("?").append(queryString);
            }
            
            logMsg.append(" | IP: ").append(clientIp);
            
            if (handlerInfo != null) {
                logMsg.append(" | Handler: ").append(handlerInfo);
            }
            
            if (!params.isEmpty()) {
                logMsg.append(" | Params: ").append(maskSensitiveParams(params));
            }

            log.info(logMsg.toString());
        } catch (Exception e) {
            // 记录日志时出错不应该影响业务
            log.warn("记录请求日志失败", e);
        }
    }

    /**
     * 记录响应信息
     */
    private void logResponse(HttpServletRequest request, HttpServletResponse response, 
                            Object handler, long duration, Exception ex) {
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            int status = response.getStatus();

            StringBuilder logMsg = new StringBuilder();
            logMsg.append("请求结束: [").append(method).append("] ").append(uri);
            logMsg.append(" | Status: ").append(status);
            logMsg.append(" | Duration: ").append(duration).append("ms");

            if (ex != null) {
                logMsg.append(" | Exception: ").append(ex.getClass().getSimpleName());
                if (ex.getMessage() != null) {
                    logMsg.append(" - ").append(ex.getMessage());
                }
            }

            log.info(logMsg.toString());
        } catch (Exception e) {
            // 记录日志时出错不应该影响业务
            log.warn("记录响应日志失败", e);
        }
    }

    /**
     * 获取客户端 IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个 IP 的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }

    /**
     * 获取请求参数（Query 参数）
     */
    private Map<String, String> getRequestParams(HttpServletRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                // 如果多个值，用逗号连接
                params.put(key, String.join(",", values));
            }
        }
        
        return params;
    }

    /**
     * 脱敏处理敏感参数
     */
    private Map<String, String> maskSensitiveParams(Map<String, String> params) {
        Map<String, String> maskedParams = new LinkedHashMap<>();
        
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String value = entry.getValue();
            
            // 检查是否是敏感字段
            boolean isSensitive = SENSITIVE_FIELDS.stream()
                    .anyMatch(sensitive -> key.contains(sensitive));
            
            if (isSensitive && value != null && !value.isEmpty()) {
                // 脱敏：只显示前2个字符和后2个字符，中间用***代替
                if (value.length() <= 4) {
                    maskedParams.put(entry.getKey(), "****");
                } else {
                    maskedParams.put(entry.getKey(), 
                            value.substring(0, 2) + "***" + value.substring(value.length() - 2));
                }
            } else {
                maskedParams.put(entry.getKey(), value);
            }
        }
        
        return maskedParams;
    }

    /**
     * 获取处理器信息（类名.方法名）
     */
    private String getHandlerInfo(Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            String className = handlerMethod.getBeanType().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();
            return className + "." + methodName;
        }
        return null;
    }
}