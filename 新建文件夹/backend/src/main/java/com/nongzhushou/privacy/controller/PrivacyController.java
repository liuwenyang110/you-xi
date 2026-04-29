package com.nongzhushou.privacy.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.privacy.service.PrivacyService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 隐私合规控制器
 * 提供用户隐私同意管理接口，满足 PIPL 合规
 */
@RestController
@Validated
@RequestMapping("/api/v1/privacy")
public class PrivacyController {

    private final PrivacyService privacyService;

    public PrivacyController(PrivacyService privacyService) {
        this.privacyService = privacyService;
    }

    /**
     * 记录用户同意
     * 前端在用户同意隐私政策、位置授权等场景调用
     */
    @PostMapping("/consent")
    public Result<Map<String, Object>> recordConsent(
            @RequestParam String consentType,
            @RequestParam(required = false, defaultValue = "1.0") String consentVersion,
            HttpServletRequest request
    ) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return Result.ok(privacyService.recordConsent(consentType, consentVersion, ipAddress, userAgent));
    }

    /**
     * 检查用户是否已同意
     */
    @GetMapping("/consent/check")
    public Result<Map<String, Object>> checkConsent(@RequestParam String consentType) {
        boolean consented = privacyService.hasConsented(consentType);
        return Result.ok(Map.of("consentType", consentType, "consented", consented));
    }

    /**
     * 获取当前用户所有同意记录
     */
    @GetMapping("/consents")
    public Result<List<Map<String, Object>>> listConsents() {
        return Result.ok(privacyService.listConsents());
    }

    /**
     * 撤回同意（PIPL 赋予用户的撤回权）
     */
    @PostMapping("/consent/revoke")
    public Result<Map<String, Object>> revokeConsent(@RequestParam String consentType) {
        return Result.ok(privacyService.revokeConsent(consentType));
    }
}
