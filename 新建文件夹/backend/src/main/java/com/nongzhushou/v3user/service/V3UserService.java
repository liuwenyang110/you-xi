package com.nongzhushou.v3user.service;

import com.nongzhushou.common.exception.BizException;
import com.nongzhushou.common.exception.ErrorCode;
import com.nongzhushou.common.security.AuthContext;
import com.nongzhushou.v3user.dto.V3JoinZoneRequest;
import com.nongzhushou.v3user.dto.V3SelectRoleRequest;
import com.nongzhushou.v3user.entity.V3UserEntity;
import com.nongzhushou.v3user.mapper.V3UserMapper;
import com.nongzhushou.zone.mapper.ZoneMapper;
import com.nongzhushou.zone.entity.ZoneEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class V3UserService {

    // 片区切换冷却期（天）
    private static final int ZONE_CHANGE_COOLDOWN_DAYS = 7;

    @Autowired private V3UserMapper v3UserMapper;
    @Autowired private ZoneMapper zoneMapper;

    /**
     * 首次选择角色（不可更改）
     */
    @Transactional
    public V3UserEntity selectRole(V3SelectRoleRequest req) {
        Long accountId = AuthContext.currentUserIdOrDefault(null);
        if (accountId == null) throw new BizException(ErrorCode.UNAUTHORIZED);

        // 检查是否已经选过角色
        V3UserEntity existing = v3UserMapper.findByAccountId(accountId);
        if (existing != null) {
            throw new BizException(ErrorCode.BIZ_ERROR, "您已完成角色选择，不可更改");
        }

        V3UserEntity user = new V3UserEntity();
        user.setAccountId(accountId);
        user.setRole(req.getRole());
        user.setRealName(req.getRealName());
        user.setHomeLocation(req.getHomeLocation());
        user.setVerified(0);
        user.setStatus("NORMAL");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        v3UserMapper.insert(user);
        return user;
    }

    /**
     * 加入片区（农机手只能加入一个片区）
     */
    @Transactional
    public V3UserEntity joinZone(V3JoinZoneRequest req) {
        V3UserEntity user = getCurrentV3User();

        // 检查片区存在
        ZoneEntity zone = zoneMapper.findById(req.getZoneId());
        if (zone == null || !"ACTIVE".equals(zone.getStatus())) {
            throw new BizException(ErrorCode.BIZ_ERROR, "片区不存在或已停用");
        }

        // 首次加入
        if (user.getZoneId() == null) {
            user.setZoneId(req.getZoneId());
            user.setZoneJoinedAt(LocalDateTime.now());
        } else if (user.getZoneId().equals(req.getZoneId())) {
            throw new BizException(ErrorCode.BIZ_ERROR, "您已在该片区中");
        } else {
            // 切换片区：检查冷却期
            if (user.getZoneChangedAt() != null) {
                long daysSinceChange = ChronoUnit.DAYS.between(user.getZoneChangedAt(), LocalDateTime.now());
                if (daysSinceChange < ZONE_CHANGE_COOLDOWN_DAYS) {
                    throw new BizException(ErrorCode.BIZ_ERROR,
                            "切换片区需等待 " + (ZONE_CHANGE_COOLDOWN_DAYS - daysSinceChange) + " 天");
                }
            }
            Long oldZoneId = user.getZoneId();
            user.setZoneId(req.getZoneId());
            user.setZoneChangedAt(LocalDateTime.now());
            // 刷新旧片区统计
            zoneMapper.refreshStats(oldZoneId);
        }

        user.setUpdatedAt(LocalDateTime.now());
        v3UserMapper.updateById(user);

        // 刷新新片区统计
        zoneMapper.refreshStats(req.getZoneId());
        return user;
    }

    /**
     * 获取当前用户 V3 信息
     */
    public Map<String, Object> getProfile() {
        V3UserEntity user = getCurrentV3User();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("role", user.getRole());
        result.put("roleName", "FARMER".equals(user.getRole()) ? "农户" : "农机手");
        result.put("realName", user.getRealName());
        result.put("verified", user.getVerified());
        result.put("zoneId", user.getZoneId());
        result.put("homeLocation", user.getHomeLocation());
        result.put("status", user.getStatus());
        result.put("createdAt", user.getCreatedAt());
        return result;
    }

    /**
     * 获取当前登录用户的 V3User（未选角色则返回 null）
     */
    public V3UserEntity getCurrentV3UserOrNull() {
        Long accountId = AuthContext.currentUserIdOrDefault(null);
        if (accountId == null) return null;
        return v3UserMapper.findByAccountId(accountId);
    }

    /**
     * 获取当前 V3User（断言已存在）
     */
    public V3UserEntity getCurrentV3User() {
        Long accountId = AuthContext.currentUserIdOrDefault(null);
        if (accountId == null) throw new BizException(ErrorCode.UNAUTHORIZED);
        V3UserEntity user = v3UserMapper.findByAccountId(accountId);
        if (user == null) throw new BizException(ErrorCode.BIZ_ERROR, "请先完成角色选择");
        return user;
    }

    /**
     * 获取片区内的农机手列表（仅本片区农户可见）
     */
    public List<V3UserEntity> getZoneOperators(Long zoneId) {
        // 验证当前用户在该片区
        V3UserEntity me = getCurrentV3User();
        if (!"FARMER".equals(me.getRole())) {
            throw new BizException(ErrorCode.BIZ_ERROR, "仅农户可查看片区农机手信息");
        }
        if (!zoneId.equals(me.getZoneId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "仅可查看本片区的农机手信息");
        }
        return v3UserMapper.findOperatorsByZone(zoneId);
    }
    /**
     * 管理员查询用户列表
     */
    public Map<String, Object> adminListUsers(String role, int page, int size) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<V3UserEntity> list = v3UserMapper.findByRolePaged(role, (page - 1) * size, size);
        long total = v3UserMapper.countByRole(role);
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    /**
     * 管理员修改用户状态
     */
    public void adminUpdateUserStatus(Long userId, String status) {
        V3UserEntity user = v3UserMapper.selectById(userId);
        if (user == null) throw new BizException(ErrorCode.BIZ_ERROR, "用户不存在");
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        v3UserMapper.updateById(user);
    }

    /**
     * 数据看板聚合数据
     */
    public Map<String, Object> getDashboard() {
        Map<String, Object> result = new LinkedHashMap<>();
        // 概览统计
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("farmerCount", v3UserMapper.countByRole("FARMER"));
        overview.put("operatorCount", v3UserMapper.countByRole("OPERATOR"));
        result.put("overview", overview);
        return result;
    }

    /**
     * 管理员查询所有需求（简单分页）
     */
    public Map<String, Object> adminListDemands(int page, int size) {
        // 占位实现，对接 V3DemandMapper 后补全
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", new java.util.ArrayList<>());
        result.put("total", 0);
        return result;
    }
}
