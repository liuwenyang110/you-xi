package com.nongzhushou.contact.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.contact.service.ContactRevealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 公益联系信息授权控制器 — V3 公益直联
 *
 * 核心理念：
 * 平台仅提供信息对接渠道，不参与任何商业交易。
 * 本控制器记录"农户查看了服务者联系方式"的授权行为，
 * 一方面保护服务者隐私，另一方面统计平台公益对接数据。
 *
 * API 路径：/api/v3/contact-reveal
 */
@RestController
@RequestMapping("/api/v3/contact-reveal")
public class ContactRevealController {

    @Autowired
    private ContactRevealService revealService;

    /**
     * 记录联系方式查看行为（授权日志）
     *
     * 调用时机：农户点击"一键拨打"或"查看联系方式"时触发
     *
     * @param operatorId 被联系的服务者 ID
     * @param source     联系来源（DEMAND_LIST / OPERATOR_DETAIL / ZONE_HOME / HOME）
     */
    @PostMapping("/log")
    public Result<Map<String, Object>> logReveal(
            @RequestParam Long operatorId,
            @RequestParam(defaultValue = "OPERATOR_DETAIL") String source
    ) {
        return Result.ok(revealService.logReveal(operatorId, source));
    }

    /**
     * 查询当前用户今日联系次数（防骚扰友好提示）
     *
     * 公益原则：本平台不强制限制，仅提供建议性提醒。
     */
    @GetMapping("/today-count")
    public Result<Map<String, Object>> todayRevealCount() {
        return Result.ok(revealService.getTodayCount());
    }

    /**
     * 管理员查询片区联系统计（公益看板数据源）
     *
     * @param zoneId 片区 ID
     */
    @GetMapping("/stats/zone/{zoneId}")
    public Result<Map<String, Object>> zoneRevealStats(@PathVariable Long zoneId) {
        return Result.ok(revealService.getZoneStats(zoneId));
    }

    /**
     * 全平台公益统计（首页数据源）
     *
     * 返回平台整体公益对接数据，用于公益首页实时统计展示
     */
    @GetMapping("/stats/platform")
    public Result<Map<String, Object>> platformStats() {
        return Result.ok(revealService.getPlatformStats());
    }
}
