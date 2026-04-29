package com.nongzhushou.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nongzhushou.auth.dto.RealnameSubmitRequest;
import com.nongzhushou.auth.dto.RoleSwitchRequest;
import com.nongzhushou.auth.dto.SmsLoginRequest;
import com.nongzhushou.auth.dto.UiModeSwitchRequest;
import com.nongzhushou.auth.dto.WechatLoginRequest;
import com.nongzhushou.auth.security.JwtTokenService;
import com.nongzhushou.auth.service.AuthService;
import com.nongzhushou.common.api.AliyunSmsClient;
import com.nongzhushou.common.enums.RoleType;
import com.nongzhushou.common.security.AuthContext;
import com.nongzhushou.common.enums.UiMode;
import com.nongzhushou.common.store.DemoDataStore;
import com.nongzhushou.common.util.RedisKeys;
import com.nongzhushou.user.entity.UserAccountEntity;
import com.nongzhushou.user.mapper.UserAccountMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final DemoDataStore demoDataStore;
    private final UserAccountMapper userAccountMapper;
    private final StringRedisTemplate redisTemplate;
    private final JwtTokenService jwtTokenService;
    private final AliyunSmsClient aliyunSmsClient;

    @Value("${app.auth.mock-token-prefix}")
    private String mockTokenPrefix;

    public AuthServiceImpl(
            DemoDataStore demoDataStore,
            UserAccountMapper userAccountMapper,
            StringRedisTemplate redisTemplate,
            JwtTokenService jwtTokenService,
            AliyunSmsClient aliyunSmsClient
    ) {
        this.demoDataStore = demoDataStore;
        this.userAccountMapper = userAccountMapper;
        this.redisTemplate = redisTemplate;
        this.jwtTokenService = jwtTokenService;
        this.aliyunSmsClient = aliyunSmsClient;
    }

    @Override
    public Map<String, Object> sendSmsCode(String phone) {
        // 生成6位随机验证码
        String code = String.valueOf(100000 + new java.util.Random().nextInt(900000));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("phone", phone);
        data.put("message", "验证码已发送");
        // dev 环境提示验证码（方便调试），prod 不返回
        if (mockTokenPrefix != null && !mockTokenPrefix.isEmpty()) {
            data.put("smsCode", code);
        }
        try {
            redisTemplate.opsForValue().set(RedisKeys.smsCode(phone), code, Duration.ofMinutes(5));
        } catch (Exception ignored) {
        }

        // 真实发送阿里云短信（如 AKSK 已配置）
        if (aliyunSmsClient.isConfigured()) {
            aliyunSmsClient.sendVerificationCode(phone, code);
        }

        return data;
    }

    @Override
    public Map<String, Object> loginBySms(SmsLoginRequest request) {
        String inputCode = request.getCode();
        String redisCode = null;
        try {
            redisCode = redisTemplate.opsForValue().get(RedisKeys.smsCode(request.getPhone()));
        } catch (Exception ignored) {
        }
        // dev 环境允许 123456 万能验证码，prod 环境严格校验 Redis
        boolean isDevMode = mockTokenPrefix != null && !mockTokenPrefix.isEmpty();
        boolean devBypass = isDevMode && "123456".equals(inputCode);
        if (!devBypass && (redisCode == null || !redisCode.equals(inputCode))) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        UserAccountEntity user = userAccountMapper.selectOne(
                new LambdaQueryWrapper<UserAccountEntity>()
                        .eq(UserAccountEntity::getPhone, request.getPhone())
                        .last("limit 1")
        );
        if (user == null) {
            Map<String, Object> fallback = demoDataStore.getUser("13800000002".equals(request.getPhone()) ? 10002L : 10001L);
            Map<String, Object> data = new LinkedHashMap<>(fallback);
            data.put("token", mockTokenPrefix + request.getPhone());
            return data;
        }

        String currentRole = user.getCurrentRole() == null ? user.getPrimaryRole() : user.getCurrentRole();
        RoleType roleType = RoleType.valueOf(currentRole.toUpperCase());
        String token = jwtTokenService.generateToken(user.getId(), roleType);
        String uiMode = readUiMode(user.getId());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", user.getId());
        data.put("phone", user.getPhone());
        data.put("currentRole", currentRole);
        data.put("status", user.getStatus());
        data.put("uiMode", uiMode);
        data.put("token", token);
        data.put("mockToken", mockTokenPrefix + request.getPhone());
        return data;
    }

    @Override
    public Map<String, Object> loginByWechat(WechatLoginRequest request) {
        String mockOpenid = "openid_" + request.getCode(); // 模拟微信 code 换 openid
        
        UserAccountEntity user = userAccountMapper.selectOne(
                new LambdaQueryWrapper<UserAccountEntity>()
                        .eq(UserAccountEntity::getWxOpenid, mockOpenid)
                        .last("limit 1")
        );
        
        boolean isNewUser = false;
        if (user == null) {
            isNewUser = true;
            user = new UserAccountEntity();
            user.setWxOpenid(mockOpenid);
            user.setPhone("wx_" + System.currentTimeMillis() % 1000000); // 虚拟手机号
            user.setPrimaryRole(RoleType.FARMER.name());
            user.setCurrentRole(RoleType.FARMER.name());
            user.setStatus("NORMAL");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            // 设置昵称头像
            user.setNickname(request.getNickname() != null ? request.getNickname() : "微信用户");
            user.setAvatarUrl(request.getAvatarUrl());
            userAccountMapper.insert(user);
        } else {
            // 更新一下昵称头像
            boolean updated = false;
            if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
                user.setNickname(request.getNickname());
                updated = true;
            }
            if (request.getAvatarUrl() != null && !request.getAvatarUrl().equals(user.getAvatarUrl())) {
                user.setAvatarUrl(request.getAvatarUrl());
                updated = true;
            }
            if (updated) {
                user.setUpdatedAt(LocalDateTime.now());
                userAccountMapper.updateById(user);
            }
        }

        String currentRole = user.getCurrentRole() == null ? user.getPrimaryRole() : user.getCurrentRole();
        RoleType roleType = RoleType.valueOf(currentRole.toUpperCase());
        String token = jwtTokenService.generateToken(user.getId(), roleType);
        
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", user.getId());
        data.put("currentRole", currentRole);
        data.put("token", token);
        data.put("isNewUser", isNewUser);
        data.put("nickname", user.getNickname());
        data.put("avatarUrl", user.getAvatarUrl());
        data.put("uiMode", readUiMode(user.getId()));
        return data;
    }

    @Override
    public Map<String, Object> submitRealname(RealnameSubmitRequest request) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("realName", request.getRealName());
        data.put("idCardNoMasked", maskIdCard(request.getIdCardNo()));
        data.put("authStatus", "PENDING");
        return data;
    }

    @Override
    public Map<String, Object> switchRole(RoleSwitchRequest request) {
        Long userId = currentUserId();
        UserAccountEntity user = userAccountMapper.selectById(userId);
        if (user != null) {
            user.setCurrentRole(request.getRole().toUpperCase());
            userAccountMapper.updateById(user);
            return Map.of("userId", userId, "currentRole", user.getCurrentRole());
        }
        return demoDataStore.updateUserRole(userId, request.getRole());
    }

    @Override
    public Map<String, Object> switchUiMode(UiModeSwitchRequest request) {
        Long userId = currentUserId();
        String uiMode = request.getUiMode().toUpperCase();
        try {
            redisTemplate.opsForValue().set(RedisKeys.userUiMode(userId), uiMode, Duration.ofDays(30));
            return Map.of("userId", userId, "uiMode", uiMode);
        } catch (Exception ignored) {
            return demoDataStore.updateUserUiMode(userId, request.getUiMode());
        }
    }

    @Override
    public Map<String, Object> currentUser() {
        Long userId = currentUserId();
        UserAccountEntity user = userAccountMapper.selectById(userId);
        if (user == null) {
            return demoDataStore.getUser(userId);
        }
        return Map.of(
                "userId", user.getId(),
                "phone", user.getPhone(),
                "currentRole", user.getCurrentRole(),
                "status", user.getStatus(),
                "uiMode", readUiMode(userId)
        );
    }

    private Long currentUserId() {
        return AuthContext.currentUserId();
    }

    private String readUiMode(Long userId) {
        try {
            String uiMode = redisTemplate.opsForValue().get(RedisKeys.userUiMode(userId));
            return uiMode == null ? UiMode.NORMAL.name() : uiMode;
        } catch (Exception ignored) {
            return UiMode.NORMAL.name();
        }
    }

    private String maskIdCard(String idCardNo) {
        if (idCardNo == null || idCardNo.length() < 8) {
            return "****";
        }
        return idCardNo.substring(0, 4) + "**********" + idCardNo.substring(idCardNo.length() - 4);
    }
}
