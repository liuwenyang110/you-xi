package com.nongzhushou.contact.service;

import com.nongzhushou.contact.mapper.ContactRevealMapper;
import com.nongzhushou.contact.entity.ContactRevealLogEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 公益联系行为记录 Service
 *
 * 职责：
 * 1. 记录农户"查看服务者联系方式"的行为日志（contact_reveal_log 表）
 * 2. 提供今日联系次数查询（防骚扰友好提示）
 * 3. 聚合片区/平台公益对接统计数据（Admin 看板 + 公益首页）
 */
@Service
public class ContactRevealService {

    @Autowired
    private ContactRevealMapper revealMapper;

    /**
     * 记录联系行为
     *
     * @param operatorId 被联系的服务者 ID
     * @param source     来源页面（OPERATOR_DETAIL / ZONE_HOME / DEMAND_LIST / HOME）
     * @return 操作结果，包含日志 ID
     */
    public Map<String, Object> logReveal(Long operatorId, String source) {
        Long farmerId = getCurrentUserId();

        ContactRevealLogEntity log = new ContactRevealLogEntity();
        log.setFarmerId(farmerId);
        log.setOperatorId(operatorId);
        log.setSource(source);
        // zone_id 从 operator 档案中查询（可选优化，当前允许 null）

        revealMapper.insert(log);

        Map<String, Object> result = new HashMap<>();
        result.put("logged", true);
        result.put("logId", log.getId());
        result.put("message", "联系行为已记录，感谢使用公益农助手平台");
        return result;
    }

    /**
     * 查询当前用户今日联系次数
     *
     * @return 今日联系次数 + 建议文案
     */
    public Map<String, Object> getTodayCount() {
        Long farmerId = getCurrentUserId();
        int count = revealMapper.countTodayByFarmer(farmerId, LocalDate.now());

        Map<String, Object> result = new HashMap<>();
        result.put("todayCount", count);
        result.put("suggestion", count == 0
                ? "您今日还未联系过服务者，可以从片区列表开始寻找帮助"
                : count >= 20
                ? "您今日已联系 " + count + " 位服务者，建议先与已联系的服务者沟通确认"
                : "您今日已联系 " + count + " 位服务者");
        return result;
    }

    /**
     * 查询片区公益对接统计
     *
     * @param zoneId 片区 ID
     * @return 该片区今日/本月/历史联系统计
     */
    public Map<String, Object> getZoneStats(Long zoneId) {
        LocalDate today = LocalDate.now();
        Map<String, Object> stats = new HashMap<>();
        stats.put("zoneId", zoneId);
        stats.put("todayRevealCount",   revealMapper.countByZoneAndDate(zoneId, today, today));
        stats.put("monthRevealCount",   revealMapper.countByZoneAndDate(zoneId, today.withDayOfMonth(1), today));
        stats.put("totalRevealCount",   revealMapper.countTotalByZone(zoneId));
        stats.put("uniqueOperatorsContacted", revealMapper.countUniqueOperatorsByZone(zoneId, today));
        return stats;
    }

    /**
     * 查询全平台公益统计（公益首页实时展示）
     *
     * 先查 platform_daily_stats 缓存，无缓存则实时聚合（允许轻微延迟）
     *
     * @return 平台公益 KPI 指标
     */
    public Map<String, Object> getPlatformStats() {
        LocalDate today = LocalDate.now();
        Map<String, Object> stats = new HashMap<>();

        // 尝试读缓存快照表
        Map<String, Object> cached = revealMapper.findDailyStats(today);
        if (cached != null) {
            stats.putAll(cached);
        } else {
            // 实时聚合（后台 Job 未跑时降级）
            stats.put("todayDemands",   revealMapper.countTodayDemands(today));
            stats.put("todayConnected", revealMapper.countByZoneAndDate(null, today, today));
            stats.put("totalOperators", revealMapper.countTotalOperators());
            stats.put("coveredZones",   revealMapper.countCoveredZones(today));
        }
        stats.put("platformMission", "免费信息对接，不收取任何费用，价格由双方直接协商");
        return stats;
    }

    // ──────────────────────────────────────────
    // 工具方法
    // ──────────────────────────────────────────

    /** 从 Spring Security 上下文中获取当前登录用户 ID */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("用户未登录");
        }
        // 根据项目实际 UserDetails 实现调整
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            try {
                return Long.parseLong(ud.getUsername());
            } catch (NumberFormatException ignored) {}
        }
        throw new IllegalStateException("无法获取用户 ID");
    }
}
