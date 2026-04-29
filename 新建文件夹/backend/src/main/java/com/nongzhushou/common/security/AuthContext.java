package com.nongzhushou.common.security;

import com.nongzhushou.common.enums.RoleType;
import com.nongzhushou.common.exception.BizException;
import com.nongzhushou.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthContext {

    private AuthContext() {
    }

    /**
     * 获取当前已认证用户 ID，未认证则抛出 401。
     * 正式接口应统一用此方法。
     */
    public static Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String principal && principal.matches("\\d+")) {
            return Long.valueOf(principal);
        }
        throw new BizException(ErrorCode.UNAUTHORIZED, "未登录或登录已过期，请重新登录");
    }

    /**
     * @deprecated 仅保留用于平滑迁移，新代码禁止使用。
     * 生产环境 fallback 应为 null 并在调用方处理。
     */
    @Deprecated
    public static Long currentUserIdOrDefault(Long fallbackUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String principal && principal.matches("\\d+")) {
            return Long.valueOf(principal);
        }
        return fallbackUserId;
    }

    public static RoleType currentRoleOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return null;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority == null || authority.getAuthority() == null) {
                continue;
            }
            String raw = authority.getAuthority();
            if (!raw.startsWith("ROLE_")) {
                continue;
            }
            try {
                return RoleType.valueOf(raw.substring("ROLE_".length()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    public static void requireRole(RoleType expectedRole) {
        RoleType currentRole = currentRoleOrNull();
        if (currentRole != expectedRole) {
            throw new BizException(ErrorCode.FORBIDDEN, "admin permission required");
        }
    }
}

