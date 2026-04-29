package com.nongzhushou.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLogInterceptor.class);
    private static final String START_AT = "requestStartAt";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_AT, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        Object startAt = request.getAttribute(START_AT);
        long durationMs = startAt instanceof Long ? System.currentTimeMillis() - (Long) startAt : -1L;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication != null && authentication.getPrincipal() != null
                ? String.valueOf(authentication.getPrincipal())
                : request.getHeader("X-User-Id");
        String role = authentication != null && authentication.getAuthorities() != null
                ? authentication.getAuthorities().stream().findFirst().map(Object::toString).orElse(request.getHeader("X-Role"))
                : request.getHeader("X-Role");
        log.info(
                "request {} {} status={} durationMs={} userId={} role={}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                durationMs,
                userId == null ? "-" : userId,
                role == null ? "-" : role
        );
    }
}
