package com.nongzhushou.privacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nongzhushou.common.security.AuthContext;
import com.nongzhushou.privacy.entity.PrivacyConsentEntity;
import com.nongzhushou.privacy.mapper.PrivacyConsentMapper;
import com.nongzhushou.privacy.service.PrivacyService;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PrivacyServiceImpl implements PrivacyService {

    private final PrivacyConsentMapper consentMapper;

    public PrivacyServiceImpl(PrivacyConsentMapper consentMapper) {
        this.consentMapper = consentMapper;
    }

    @Override
    public Map<String, Object> recordConsent(String consentType, String consentVersion, String ipAddress, String userAgent) {
        Long userId = AuthContext.currentUserId();

        // 查找是否已有同意记录（幂等操作）
        PrivacyConsentEntity existing = consentMapper.selectOne(
                new LambdaQueryWrapper<PrivacyConsentEntity>()
                        .eq(PrivacyConsentEntity::getUserId, userId)
                        .eq(PrivacyConsentEntity::getConsentType, consentType)
                        .last("limit 1"));

        if (existing != null) {
            // 更新现有记录
            consentMapper.update(null,
                    new LambdaUpdateWrapper<PrivacyConsentEntity>()
                            .eq(PrivacyConsentEntity::getId, existing.getId())
                            .set(PrivacyConsentEntity::getConsented, 1)
                            .set(PrivacyConsentEntity::getConsentVersion, consentVersion)
                            .set(PrivacyConsentEntity::getConsentedAt, LocalDateTime.now())
                            .set(PrivacyConsentEntity::getIpAddress, ipAddress)
                            .set(PrivacyConsentEntity::getUserAgent, userAgent));
            existing = consentMapper.selectById(existing.getId());
            return toMap(existing);
        }

        // 创建新记录
        PrivacyConsentEntity entity = new PrivacyConsentEntity();
        entity.setUserId(userId);
        entity.setConsentType(consentType);
        entity.setConsented(1);
        entity.setConsentVersion(consentVersion);
        entity.setConsentedAt(LocalDateTime.now());
        entity.setIpAddress(ipAddress);
        entity.setUserAgent(userAgent);
        consentMapper.insert(entity);
        return toMap(entity);
    }

    @Override
    public boolean hasConsented(String consentType) {
        Long userId = AuthContext.currentUserId();
        PrivacyConsentEntity consent = consentMapper.selectOne(
                new LambdaQueryWrapper<PrivacyConsentEntity>()
                        .eq(PrivacyConsentEntity::getUserId, userId)
                        .eq(PrivacyConsentEntity::getConsentType, consentType)
                        .eq(PrivacyConsentEntity::getConsented, 1)
                        .last("limit 1"));
        return consent != null;
    }

    @Override
    public List<Map<String, Object>> listConsents() {
        Long userId = AuthContext.currentUserId();
        return consentMapper.selectList(
                        new LambdaQueryWrapper<PrivacyConsentEntity>()
                                .eq(PrivacyConsentEntity::getUserId, userId)
                                .orderByAsc(PrivacyConsentEntity::getConsentType))
                .stream()
                .map(this::toMap)
                .toList();
    }

    @Override
    public Map<String, Object> revokeConsent(String consentType) {
        Long userId = AuthContext.currentUserId();
        consentMapper.update(null,
                new LambdaUpdateWrapper<PrivacyConsentEntity>()
                        .eq(PrivacyConsentEntity::getUserId, userId)
                        .eq(PrivacyConsentEntity::getConsentType, consentType)
                        .set(PrivacyConsentEntity::getConsented, 0));
        return Map.of("consentType", consentType, "revoked", true);
    }

    private Map<String, Object> toMap(PrivacyConsentEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("userId", entity.getUserId());
        map.put("consentType", entity.getConsentType());
        map.put("consented", entity.getConsented());
        map.put("consentVersion", entity.getConsentVersion());
        map.put("consentedAt", entity.getConsentedAt());
        return map;
    }
}
