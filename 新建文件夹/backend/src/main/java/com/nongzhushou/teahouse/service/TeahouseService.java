package com.nongzhushou.teahouse.service;

import com.nongzhushou.notification.service.NotificationStrategy;
import com.nongzhushou.teahouse.entity.TeahouseEntity;
import com.nongzhushou.teahouse.entity.TeahouseMessageEntity;
import com.nongzhushou.teahouse.mapper.TeahouseMapper;
import com.nongzhushou.teahouse.mapper.TeahouseMessageMapper;
import com.nongzhushou.common.exception.BizException;
import com.nongzhushou.common.exception.ErrorCode;
import com.nongzhushou.v3demand.mapper.V3DemandMapper;
import com.nongzhushou.oss.service.OssStorageStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 村级快闪茶馆服务
 *
 * 生命周期：
 *   首个需求触发开张 → 繁荣期自由交流
 *   → 连续7天无新需求 → 关闭征询公告（可延期）
 *   → 连续15天无新需求 → 强制关闭 + 阅后即焚
 */
@Service
public class TeahouseService {

    private static final Logger log = LoggerFactory.getLogger(TeahouseService.class);

    @Autowired private TeahouseMapper teahouseMapper;
    @Autowired private TeahouseMessageMapper messageMapper;
    @Autowired private NotificationStrategy notification;
    @Autowired private V3DemandMapper demandMapper;
    @Autowired private OssStorageStrategy ossStorage;

    // ============ 茶馆开张与状态管理 ============

    /**
     * 当有新需求发布时调用，自动开张或刷新茶馆活跃时间
     * 由 V3DemandService.publish() 联动调用
     */
    @Transactional
    public void onDemandPublished(Long zoneId, Long farmerId) {
        TeahouseEntity th = teahouseMapper.findByZoneId(zoneId);
        if (th == null) {
            // 首次开张
            th = new TeahouseEntity();
            th.setZoneId(zoneId);
            th.setStatus("OPEN");
            th.setLastDemandAt(LocalDateTime.now());
            th.setCreatedAt(LocalDateTime.now());
            th.setUpdatedAt(LocalDateTime.now());
            teahouseMapper.insert(th);
            // 发系统消息到茶馆
            postSystemMessage(th.getId(), "🎉 本村交流大厅已开张！大家可以在这里自由讨论农活信息、联系机手。来吧，乡亲们！");
            log.info("片区 {} 的茶馆已自动开张", zoneId);
        } else if (!"CLOSED".equals(th.getStatus())) {
            // 刷新活跃时间，如果茶馆在预警/延期状态，恢复为OPEN
            th.setLastDemandAt(LocalDateTime.now());
            if ("CLOSING_WARN".equals(th.getStatus()) || "EXTENDED".equals(th.getStatus())) {
                th.setStatus("OPEN");
                th.setWarnSentAt(null);
                th.setExtendDays(null);
                th.setExtendedBy(null);
                th.setExtendedAt(null);
                postSystemMessage(th.getId(), "🌾 新的需求来啦！茶馆继续热闹营业，打烊计划已取消！");
            }
            th.setUpdatedAt(LocalDateTime.now());
            teahouseMapper.updateById(th);
        } else {
            // 已关闭的茶馆，重新开张
            th.setStatus("OPEN");
            th.setLastDemandAt(LocalDateTime.now());
            th.setWarnSentAt(null);
            th.setExtendDays(null);
            th.setExtendedBy(null);
            th.setExtendedAt(null);
            th.setForceClosedAt(null);
            th.setUpdatedAt(LocalDateTime.now());
            teahouseMapper.updateById(th);
            postSystemMessage(th.getId(), "🎉 本村交流大厅重新开张啦！又到农忙时节了，大家有活儿赶紧吆喝起来！");
            log.info("片区 {} 的茶馆已重新开张", zoneId);
        }
    }

    // ============ 消息收发 ============

    /** 发送消息到茶馆 */
    @Transactional
    public TeahouseMessageEntity sendMessage(Long zoneId, Long senderId, String msgType, String content, String mediaKey) {
        TeahouseEntity th = teahouseMapper.findByZoneId(zoneId);
        if (th == null || "CLOSED".equals(th.getStatus())) {
            throw new BizException(ErrorCode.BIZ_ERROR, "本村交流大厅暂未开张或已关闭");
        }
        TeahouseMessageEntity msg = new TeahouseMessageEntity();
        msg.setTeahouseId(th.getId());
        msg.setSenderId(senderId);
        msg.setMsgType(msgType);
        msg.setContent(content);
        msg.setMediaKey(mediaKey);
        msg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(msg);
        return msg;
    }

    /** 获取茶馆消息列表（分页） */
    public Map<String, Object> getMessages(Long zoneId, int page, int size) {
        TeahouseEntity th = teahouseMapper.findByZoneId(zoneId);
        if (th == null) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("list", Collections.emptyList());
            empty.put("total", 0);
            empty.put("teahouseStatus", "NOT_CREATED");
            return empty;
        }
        int offset = (page - 1) * size;
        List<TeahouseMessageEntity> list = messageMapper.findByTeahouse(th.getId(), size, offset);
        int total = messageMapper.countByTeahouse(th.getId());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("teahouseStatus", th.getStatus());
        result.put("lastDemandAt", th.getLastDemandAt());
        result.put("warnSentAt", th.getWarnSentAt());
        result.put("extendDays", th.getExtendDays());
        return result;
    }

    /** 获取茶馆状态 */
    public Map<String, Object> getTeahouseStatus(Long zoneId) {
        TeahouseEntity th = teahouseMapper.findByZoneId(zoneId);
        Map<String, Object> result = new LinkedHashMap<>();
        if (th == null) {
            result.put("status", "NOT_CREATED");
            return result;
        }
        result.put("status", th.getStatus());
        result.put("lastDemandAt", th.getLastDemandAt());
        result.put("warnSentAt", th.getWarnSentAt());
        result.put("extendDays", th.getExtendDays());
        result.put("extendedAt", th.getExtendedAt());
        result.put("createdAt", th.getCreatedAt());
        return result;
    }

    // ============ 农户申请延期 ============

    @Transactional
    public void extendTeahouse(Long zoneId, Long userId, int days) {
        TeahouseEntity th = teahouseMapper.findByZoneId(zoneId);
        if (th == null || !"CLOSING_WARN".equals(th.getStatus())) {
            throw new BizException(ErrorCode.BIZ_ERROR, "当前茶馆不在预警关闭状态，无需延期");
        }
        if (days < 1 || days > 7) {
            throw new BizException(ErrorCode.BIZ_ERROR, "延期天数请在1-7天之间");
        }
        th.setStatus("EXTENDED");
        th.setExtendDays(days);
        th.setExtendedBy(userId);
        th.setExtendedAt(LocalDateTime.now());
        th.setUpdatedAt(LocalDateTime.now());
        teahouseMapper.updateById(th);
        postSystemMessage(th.getId(),
            String.format("📢 有乡亲申请了延期 %d 天，茶馆继续开放！但如果半个月内仍没有新活儿发布，大厅将最终关闭。", days));
        log.info("片区 {} 的茶馆被申请延期 {} 天", zoneId, days);
    }

    // ============ 定时任务：每天凌晨3点 ============

    /** 7天预警扫描 */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void scanTeahouseWarn() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(7);
        List<TeahouseEntity> list = teahouseMapper.findNeedWarn(deadline);
        log.info("[茶馆预警] 扫描到 {} 个需要7天预警的茶馆", list.size());
        for (TeahouseEntity th : list) {
            th.setStatus("CLOSING_WARN");
            th.setWarnSentAt(LocalDateTime.now());
            th.setUpdatedAt(LocalDateTime.now());
            teahouseMapper.updateById(th);
            postSystemMessage(th.getId(),
                "📢 【收尾倒计时】各位乡亲、机手师傅！本村已连续7天未见新的农活发布。" +
                "如有谁家还有没干完的散活，请抓紧发布！" +
                "若需要继续保留大厅，请点【申请延期】。" +
                "否则大厅将在无新需求的第15天正式关闭。");
            log.info("茶馆 {} (zone={}) 已发送7天预警", th.getId(), th.getZoneId());
        }
    }

    /** 15天强杀扫描 */
    @Scheduled(cron = "0 30 3 * * ?")
    @Transactional
    public void scanTeahouseForceClose() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(15);
        List<TeahouseEntity> list = teahouseMapper.findNeedForceClose(deadline);
        log.info("[茶馆强杀] 扫描到 {} 个需要强制关闭的茶馆", list.size());
        for (TeahouseEntity th : list) {
            forceCloseTeahouse(th);
        }
    }

    /** 强制关闭茶馆并执行阅后即焚 */
    @Transactional
    public void forceCloseTeahouse(TeahouseEntity th) {
        // 1. 获取所有媒体文件的 OSS Key（用于后续清理）
        List<String> mediaKeys = messageMapper.findMediaKeysByTeahouse(th.getId());

        // 2. 物理删除全部消息
        int deleted = messageMapper.deleteByTeahouse(th.getId());
        log.info("茶馆 {} 已物理删除 {} 条消息", th.getId(), deleted);

        // 3. 批量删除 OSS 媒体文件
        if (!mediaKeys.isEmpty()) {
            log.info("茶馆 {} 开始清理 {} 个 OSS 媒体文件", th.getId(), mediaKeys.size());
            try {
                ossStorage.batchDelete(mediaKeys);
                log.info("茶馆 {} 的 {} 个媒体文件已清理完毕", th.getId(), mediaKeys.size());
            } catch (Exception e) {
                log.warn("茶馆 {} 媒体文件清理部分失败: {}", th.getId(), e.getMessage());
            }
        }

        // 4. 更新茶馆状态
        th.setStatus("CLOSED");
        th.setForceClosedAt(LocalDateTime.now());
        th.setUpdatedAt(LocalDateTime.now());
        teahouseMapper.updateById(th);
        log.info("茶馆 {} (zone={}) 已强制关闭，阅后即焚完毕", th.getId(), th.getZoneId());
    }

    // ============ 工具方法 ============

    private void postSystemMessage(Long teahouseId, String content) {
        TeahouseMessageEntity msg = new TeahouseMessageEntity();
        msg.setTeahouseId(teahouseId);
        msg.setSenderId(null); // 系统消息
        msg.setMsgType("SYSTEM");
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(msg);
    }
}
