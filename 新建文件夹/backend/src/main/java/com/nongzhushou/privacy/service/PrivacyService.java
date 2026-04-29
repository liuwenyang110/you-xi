package com.nongzhushou.privacy.service;

import java.util.List;
import java.util.Map;

/**
 * 隐私合规服务接口
 * 管理用户隐私同意记录，满足 PIPL 合规要求
 */
public interface PrivacyService {

    /**
     * 记录用户隐私同意
     * @param consentType 同意类型: PRIVACY_POLICY / LOCATION / PHONE_DISPLAY
     * @param consentVersion 同意的政策版本号
     * @param ipAddress 用户IP
     * @param userAgent 用户设备信息
     * @return 同意记录
     */
    Map<String, Object> recordConsent(String consentType, String consentVersion, String ipAddress, String userAgent);

    /**
     * 检查用户是否已同意指定类型
     * @param consentType 同意类型
     * @return 是否已同意
     */
    boolean hasConsented(String consentType);

    /**
     * 获取用户所有同意记录
     * @return 同意记录列表
     */
    List<Map<String, Object>> listConsents();

    /**
     * 撤回同意（PIPL 要求用户可撤回）
     * @param consentType 同意类型
     * @return 撤回结果
     */
    Map<String, Object> revokeConsent(String consentType);
}
