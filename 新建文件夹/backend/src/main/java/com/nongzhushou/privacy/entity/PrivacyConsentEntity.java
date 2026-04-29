package com.nongzhushou.privacy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 用户隐私同意记录实体
 * 满足《个人信息保护法》(PIPL) 合规要求
 */
@TableName("privacy_consent")
public class PrivacyConsentEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String consentType;       // PRIVACY_POLICY / LOCATION / PHONE_DISPLAY
    private Integer consented;        // 0否 1是
    private String consentVersion;    // 隐私政策版本号
    private LocalDateTime consentedAt;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getConsentType() { return consentType; }
    public void setConsentType(String consentType) { this.consentType = consentType; }
    public Integer getConsented() { return consented; }
    public void setConsented(Integer consented) { this.consented = consented; }
    public String getConsentVersion() { return consentVersion; }
    public void setConsentVersion(String consentVersion) { this.consentVersion = consentVersion; }
    public LocalDateTime getConsentedAt() { return consentedAt; }
    public void setConsentedAt(LocalDateTime consentedAt) { this.consentedAt = consentedAt; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
