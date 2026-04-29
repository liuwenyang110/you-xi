package com.nongzhushou.lifecycle.service;

import com.nongzhushou.notification.service.NotificationStrategy;
import com.nongzhushou.v3demand.entity.V3DemandEntity;
import com.nongzhushou.v3demand.mapper.V3DemandMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 需求生命周期管理服务
 *
 * 防僵尸"夺命连环问"时间线：
 *   T+2天  → 第一次询问
 *   T+4天  → 第二次询问
 *   两次未回复 → 最后通牒（1天后强杀）
 *   T+5天  → 系统强制清理
 */
@Service
public class DemandLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(DemandLifecycleService.class);

    @Autowired private V3DemandMapper demandMapper;
    @Autowired private NotificationStrategy notification;

    // ============ 定时任务：每天凌晨1点执行 ============

    /** 第一轮：发送首问（发布满2天且未询问过） */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void scanFirstAsk() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(2);
        List<V3DemandEntity> list = demandMapper.findNeedFirstAsk(deadline);
        log.info("[需求首问] 扫描到 {} 条需满2天未问的需求", list.size());
        for (V3DemandEntity d : list) {
            d.setFirstAskAt(LocalDateTime.now());
            demandMapper.updateById(d);
            notification.send(
                d.getFarmerId(), "DEMAND_ASK",
                "🔔 您的农活干完了吗？",
                "老乡，您发布的农事需求已经过了2天了，找到机手了吗？干完了直接点确认，咱们好给别的机手腾位子！",
                "DEMAND", d.getId(), true, "CONFIRM_COMPLETE"
            );
        }
    }

    /** 第二轮：发送二问（首问满2天且未回复） */
    @Scheduled(cron = "0 5 1 * * ?")
    @Transactional
    public void scanSecondAsk() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(2);
        List<V3DemandEntity> list = demandMapper.findNeedSecondAsk(deadline);
        log.info("[需求二问] 扫描到 {} 条首问未回复的需求", list.size());
        for (V3DemandEntity d : list) {
            d.setSecondAskAt(LocalDateTime.now());
            demandMapper.updateById(d);
            notification.send(
                d.getFarmerId(), "DEMAND_ASK",
                "🔔 再次确认：您的地干完了没？",
                "老乡，这是第二次问啦！如果活儿已经干完，请点一下确认，好让别的机手不白跑一趟。若还没找到机手就无需操作，咱们继续帮您挂着！",
                "DEMAND", d.getId(), true, "CONFIRM_COMPLETE"
            );
        }
    }

    /** 第三轮：发送最后通牒（二问后仍未回复） */
    @Scheduled(cron = "0 10 1 * * ?")
    @Transactional
    public void scanFinalWarn() {
        List<V3DemandEntity> list = demandMapper.findNeedFinalWarn();
        log.info("[最后通牒] 扫描到 {} 条两次未回复的需求", list.size());
        for (V3DemandEntity d : list) {
            d.setFinalWarnAt(LocalDateTime.now());
            demandMapper.updateById(d);
            notification.send(
                d.getFarmerId(), "DEMAND_WARN",
                "⚠️ 最后通知：您的需求明天将被清理！",
                "叮！由于您多次未答复系统询问，若1天内仍无回应，平台将认定该活儿已在线下完工，并替您强制清理该需求！如活儿确实还没干，请立即点击回复。",
                "DEMAND", d.getId(), true, "CONFIRM_COMPLETE"
            );
        }
    }

    /** 第四轮（凌晨2点）：强杀清理 */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void scanAutoClean() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(1);
        List<V3DemandEntity> list = demandMapper.findNeedAutoClean(deadline);
        log.info("[系统强杀] 扫描到 {} 条需要强制清理的需求", list.size());
        for (V3DemandEntity d : list) {
            d.setStatus("CANCELLED");
            d.setAutoCleaned(1);
            d.setUpdatedAt(LocalDateTime.now());
            demandMapper.updateById(d);
            notification.send(
                d.getFarmerId(), "DEMAND_WARN",
                "📋 您的需求已被系统清理",
                "由于您长时间未回复系统确认，该需求已被平台自动清理归档。如有误操作，可重新发布新的需求。",
                "DEMAND", d.getId(), false, null
            );
            log.info("需求 {} 已被系统强制清理", d.getId());
        }
    }

    // ============ 农户主动确认 API 调用的方法 ============

    /** 农户点击"已完成"时调用 */
    @Transactional
    public void confirmCompleted(Long demandId, Long farmerId) {
        V3DemandEntity d = demandMapper.selectById(demandId);
        if (d == null || !d.getFarmerId().equals(farmerId)) return;

        // 判断当前是第几轮询问，记录回复
        if (d.getSecondAskAt() != null && d.getSecondAskReply() == null) {
            d.setSecondAskReply("COMPLETED");
        } else if (d.getFirstAskAt() != null && d.getFirstAskReply() == null) {
            d.setFirstAskReply("COMPLETED");
        }
        d.setStatus("COMPLETED");
        d.setUpdatedAt(LocalDateTime.now());
        demandMapper.updateById(d);
        log.info("农户 {} 主动确认需求 {} 已完成", farmerId, demandId);
    }

    /** 农户点击"还没完"时调用 */
    @Transactional
    public void confirmOngoing(Long demandId, Long farmerId) {
        V3DemandEntity d = demandMapper.selectById(demandId);
        if (d == null || !d.getFarmerId().equals(farmerId)) return;

        if (d.getSecondAskAt() != null && d.getSecondAskReply() == null) {
            d.setSecondAskReply("ONGOING");
        } else if (d.getFirstAskAt() != null && d.getFirstAskReply() == null) {
            d.setFirstAskReply("ONGOING");
        }
        d.setUpdatedAt(LocalDateTime.now());
        demandMapper.updateById(d);
        log.info("农户 {} 确认需求 {} 仍在进行中", farmerId, demandId);
    }
}
