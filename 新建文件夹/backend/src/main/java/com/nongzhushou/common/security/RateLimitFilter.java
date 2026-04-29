package com.nongzhushou.common.security;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 基于 Redis 的简易接口限流过滤器。
 * 对短信发送接口和登录接口进行频率限制，防止恶意刷接口。
 */
@Component
@ConditionalOnBean(StringRedisTemplate.class)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // 短信接口：同一 IP 每 60 秒最多 3 次
    private static final int SMS_MAX_REQUESTS = 3;
    private static final int SMS_WINDOW_SECONDS = 60;

    // 登录接口：同一 IP 每 60 秒最多 10 次
    private static final int LOGIN_MAX_REQUESTS = 10;
    private static final int LOGIN_WINDOW_SECONDS = 60;

    public RateLimitFilter(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String clientIp = getClientIp(request);

        // 短信发送限流
        if (path.contains("/auth/sms/send")) {
            if (isRateLimited(clientIp, "sms", SMS_MAX_REQUESTS, SMS_WINDOW_SECONDS)) {
                writeRateLimitResponse(response, "短信发送过于频繁，请稍后再试");
                return;
            }
        }

        // 登录接口限流
        if (path.contains("/auth/login")) {
            if (isRateLimited(clientIp, "login", LOGIN_MAX_REQUESTS, LOGIN_WINDOW_SECONDS)) {
                writeRateLimitResponse(response, "登录尝试过于频繁，请稍后再试");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String clientIp, String action, int maxRequests, int windowSeconds) {
        String key = "rate_limit:" + action + ":" + clientIp;
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
            }
            if (count != null && count > maxRequests) {
                log.warn("限流触发 - IP: {}, 操作: {}, 请求次数: {}", clientIp, action, count);
                return true;
            }
        } catch (Exception e) {
            // Redis 不可用时放行，不阻塞业务
            log.error("限流检查失败，Redis 可能不可用", e);
        }
        return false;
    }

    private void writeRateLimitResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.fail(ErrorCode.RATE_LIMITED, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}
