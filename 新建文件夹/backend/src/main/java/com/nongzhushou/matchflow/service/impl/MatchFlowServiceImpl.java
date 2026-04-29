package com.nongzhushou.matchflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nongzhushou.common.enums.ContactSessionStatus;
import com.nongzhushou.common.enums.DemandStatus;
import com.nongzhushou.common.enums.MatchAttemptStatus;
import com.nongzhushou.common.enums.OrderStatus;
import com.nongzhushou.common.mq.MatchMessageProducer;
import com.nongzhushou.demand.entity.DemandEntity;
import com.nongzhushou.demand.mapper.DemandMapper;
import com.nongzhushou.matchflow.mapper.MatchTaskFlowMapper;
import com.nongzhushou.matchflow.model.ContactSessionLite;
import com.nongzhushou.matchflow.model.MatchAttemptLite;
import com.nongzhushou.matchflow.model.MatchTaskLite;
import com.nongzhushou.matchflow.model.OrderLite;
import com.nongzhushou.matchflow.model.OwnerCandidate;
import com.nongzhushou.matchflow.service.MatchEngineBridgeService;
import com.nongzhushou.matchflow.service.MatchFlowService;
import com.nongzhushou.serviceitem.entity.ServiceItemEntity;
import com.nongzhushou.serviceitem.mapper.ServiceItemMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchFlowServiceImpl implements MatchFlowService {

    private static final Logger log = LoggerFactory.getLogger(MatchFlowServiceImpl.class);

    private final MatchTaskFlowMapper flowMapper;
    private final MatchEngineBridgeService bridgeService;
    private final DemandMapper demandMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final MatchMessageProducer messageProducer;

    public MatchFlowServiceImpl(
            MatchTaskFlowMapper flowMapper,
            MatchEngineBridgeService bridgeService,
            DemandMapper demandMapper,
            ServiceItemMapper serviceItemMapper,
            MatchMessageProducer messageProducer
    ) {
        this.flowMapper = flowMapper;
        this.bridgeService = bridgeService;
        this.demandMapper = demandMapper;
        this.serviceItemMapper = serviceItemMapper;
        this.messageProducer = messageProducer;
    }

    @Override
    @Transactional
    public Long startMatchFlow(Long demandId) {
        log.info("match flow start demandId={}", demandId);
        MatchTaskLite task = flowMapper.selectTaskByDemandId(demandId);
        if (task == null) {
            task = new MatchTaskLite();
            task.setDemandId(demandId);
            task.setCurrentTier(1);
            task.setStatus(DemandStatus.MATCHING.name());
            flowMapper.insertMatchTask(task);
        }
        flowMapper.updateDemandStatus(demandId, DemandStatus.MATCHING.name());
        // 异步发送 MQ 消息，由消费者执行实际匹配派单
        messageProducer.sendMatchDispatch(demandId);
        log.info("match flow dispatched to MQ demandId={}", demandId);
        return task.getId();
    }

    @Override
    @Transactional
    public Long dispatchNextAttempt(Long demandId) {
        MatchTaskLite task = flowMapper.selectTaskByDemandId(demandId);
        if (task == null) {
            log.warn("dispatch skipped because task missing demandId={}", demandId);
            return null;
        }
        Map<Integer, List<OwnerCandidate>> tierPool = bridgeService.buildTierPool(demandId);
        for (int tier = 1; tier <= 3; tier++) {
            OwnerCandidate pick = bridgeService.randomPickFromTier(tierPool, tier);
            if (pick != null) {
                DemandEntity demand = demandMapper.selectById(demandId);
                ServiceItemEntity serviceItem = pick.getServiceItemId() == null
                        ? null
                        : serviceItemMapper.selectById(pick.getServiceItemId());
                flowMapper.updateTaskTierAndStatus(task.getId(), tier, DemandStatus.MATCHING.name());
                MatchAttemptLite attempt = new MatchAttemptLite();
                attempt.setTaskId(task.getId());
                attempt.setDemandId(demandId);
                attempt.setOwnerId(pick.getOwnerId());
                attempt.setServiceItemId(pick.getServiceItemId());
                attempt.setTierNo(tier);
                attempt.setDistanceMeter(pick.getDistanceKm() == null ? null : (int) Math.round(pick.getDistanceKm() * 1000));
                attempt.setDistanceSource(pick.getDistanceSource());
                attempt.setRouteDistanceKm(pick.getDistanceKm());
                attempt.setStraightDistanceKm(pick.getStraightDistanceKm());
                attempt.setEtaMinutes(pick.getDurationMinutes() == null ? null : pick.getDurationMinutes().intValue());
                attempt.setMatchReason(pick.getReason());
                attempt.setServiceName(serviceItem == null ? "PENDING_SERVICE" : serviceItem.getServiceName());
                attempt.setVillageName(demand == null ? null : demand.getVillageName());
                attempt.setDistanceLayer(describeTier(tier));
                attempt.setStatus(MatchAttemptStatus.PENDING_RESPONSE.name());
                attempt.setOwnerResponseDeadline(LocalDateTime.now().plusSeconds(120));
                flowMapper.insertMatchAttempt(attempt);
                demandMapper.update(
                        null,
                        new LambdaUpdateWrapper<DemandEntity>()
                                .eq(DemandEntity::getId, demandId)
                                .set(DemandEntity::getCurrentMatchAttemptId, attempt.getId())
                                .set(DemandEntity::getCurrentContactSessionId, null)
                                .set(DemandEntity::getCurrentOrderId, null)
                                .setSql("match_attempt_count = COALESCE(match_attempt_count, 0) + 1")
                );
                log.info(
                        "dispatch created attempt demandId={} attemptId={} ownerId={} serviceItemId={} tier={}",
                        demandId,
                        attempt.getId(),
                        pick.getOwnerId(),
                        pick.getServiceItemId(),
                        tier
                );
                return attempt.getId();
            }
        }
        flowMapper.markTaskFailed(task.getId(), "NO_ELIGIBLE_OWNER");
        flowMapper.updateDemandStatus(demandId, DemandStatus.MATCH_FAILED.name());
        demandMapper.update(
                null,
                new LambdaUpdateWrapper<DemandEntity>()
                        .eq(DemandEntity::getId, demandId)
                        .set(DemandEntity::getCurrentMatchAttemptId, null)
                        .set(DemandEntity::getCurrentContactSessionId, null)
        );
        log.info("dispatch failed demandId={} reason=NO_ELIGIBLE_OWNER", demandId);
        return null;
    }

    @Override
    @Transactional
    public boolean ownerAccept(Long attemptId) {
        MatchAttemptLite attempt = flowMapper.selectAttemptById(attemptId);
        if (attempt == null) {
            log.warn("owner accept skipped because attempt missing attemptId={}", attemptId);
            return false;
        }
        if (MatchAttemptStatus.WAIT_FARMER_CONFIRM.name().equals(attempt.getStatus())
                || MatchAttemptStatus.CONTACT_OPENED.name().equals(attempt.getStatus())) {
            return flowMapper.selectContactSessionByAttemptId(attemptId) != null;
        }
        if (!MatchAttemptStatus.PENDING_RESPONSE.name().equals(attempt.getStatus())) {
            return false;
        }
        flowMapper.updateAttemptStatus(attemptId, MatchAttemptStatus.WAIT_FARMER_CONFIRM.name());
        flowMapper.updateDemandStatus(attempt.getDemandId(), DemandStatus.WAIT_FARMER_CONTACT_CONFIRM.name());
        ContactSessionLite session = new ContactSessionLite();
        session.setDemandId(attempt.getDemandId());
        session.setOwnerId(attempt.getOwnerId());
        session.setFarmerId(flowMapper.selectFarmerIdByDemandId(attempt.getDemandId()));
        session.setServiceItemId(attempt.getServiceItemId());
        session.setMatchAttemptId(attemptId);
        session.setSessionStatus(ContactSessionStatus.WAIT_FARMER_CONFIRM.name());
        session.setContactMode("MASKED");
        session.setExpireAt(LocalDateTime.now().plusHours(12));
        flowMapper.insertContactSession(session);
        demandMapper.update(
                null,
                new LambdaUpdateWrapper<DemandEntity>()
                        .eq(DemandEntity::getId, attempt.getDemandId())
                        .set(DemandEntity::getCurrentMatchAttemptId, attemptId)
                        .set(DemandEntity::getCurrentContactSessionId, session.getId())
        );
        log.info(
                "owner accepted attemptId={} demandId={} ownerId={} contactSessionId={}",
                attemptId,
                attempt.getDemandId(),
                attempt.getOwnerId(),
                session.getId()
        );
        return true;
    }

    @Override
    @Transactional
    public boolean ownerReject(Long attemptId) {
        MatchAttemptLite attempt = flowMapper.selectAttemptById(attemptId);
        if (attempt == null) {
            log.warn("owner reject skipped because attempt missing attemptId={}", attemptId);
            return false;
        }
        flowMapper.closeAttempt(attemptId, MatchAttemptStatus.OWNER_REJECTED.name());
        flowMapper.updateDemandStatus(attempt.getDemandId(), DemandStatus.MATCHING.name());
        demandMapper.update(
                null,
                new LambdaUpdateWrapper<DemandEntity>()
                        .eq(DemandEntity::getId, attempt.getDemandId())
                        .set(DemandEntity::getCurrentContactSessionId, null)
                        .set(DemandEntity::getCurrentOrderId, null)
        );
        Long nextAttemptId = dispatchNextAttempt(attempt.getDemandId());
        log.info("owner rejected attemptId={} demandId={} nextAttemptId={}", attemptId, attempt.getDemandId(), nextAttemptId);
        return true;
    }

    @Override
    @Transactional
    public boolean farmerConfirm(Long attemptId) {
        MatchAttemptLite attempt = flowMapper.selectAttemptById(attemptId);
        if (attempt == null || !MatchAttemptStatus.WAIT_FARMER_CONFIRM.name().equals(attempt.getStatus())) {
            log.warn("farmer confirm rejected attemptId={} status={}", attemptId, attempt == null ? "null" : attempt.getStatus());
            return false;
        }
        ContactSessionLite session = flowMapper.selectContactSessionByAttemptId(attemptId);
        if (session == null) {
            log.warn("farmer confirm skipped because contact session missing attemptId={}", attemptId);
            return false;
        }
        flowMapper.updateAttemptStatus(attemptId, MatchAttemptStatus.CONTACT_OPENED.name());
        flowMapper.updateContactSessionStatus(session.getId(), ContactSessionStatus.CONTACT_ACTIVE.name());
        flowMapper.updateDemandStatus(attempt.getDemandId(), DemandStatus.NEGOTIATING.name());
        OrderLite order = new OrderLite();
        order.setDemandId(attempt.getDemandId());
        order.setMatchAttemptId(attemptId);
        order.setFarmerId(session.getFarmerId());
        order.setOwnerId(attempt.getOwnerId());
        order.setServiceItemId(attempt.getServiceItemId());
        order.setContactSessionId(session.getId());
        order.setStatus(OrderStatus.WAIT_NEGOTIATION.name());
        flowMapper.insertOrder(order);
        demandMapper.update(
                null,
                new LambdaUpdateWrapper<DemandEntity>()
                        .eq(DemandEntity::getId, attempt.getDemandId())
                        .set(DemandEntity::getCurrentMatchAttemptId, attemptId)
                        .set(DemandEntity::getCurrentContactSessionId, session.getId())
                        .set(DemandEntity::getCurrentOrderId, order.getId())
        );
        MatchTaskLite task = flowMapper.selectTaskByDemandId(attempt.getDemandId());
        if (task != null) {
            flowMapper.markTaskSuccess(task.getId(), attempt.getOwnerId(), attempt.getServiceItemId());
        }
        log.info(
                "farmer confirmed attemptId={} demandId={} contactSessionId={} orderId={}",
                attemptId,
                attempt.getDemandId(),
                session.getId(),
                order.getId()
        );
        return true;
    }

    @Override
    @Transactional
    public boolean farmerReject(Long attemptId) {
        MatchAttemptLite attempt = flowMapper.selectAttemptById(attemptId);
        if (attempt == null) {
            log.warn("farmer reject skipped because attempt missing attemptId={}", attemptId);
            return false;
        }
        ContactSessionLite session = flowMapper.selectContactSessionByAttemptId(attemptId);
        if (session != null) {
            flowMapper.updateContactSessionStatus(session.getId(), ContactSessionStatus.CLOSED.name());
        }
        flowMapper.closeAttempt(attemptId, MatchAttemptStatus.FARMER_REJECTED.name());
        flowMapper.updateDemandStatus(attempt.getDemandId(), DemandStatus.MATCHING.name());
        demandMapper.update(
                null,
                new LambdaUpdateWrapper<DemandEntity>()
                        .eq(DemandEntity::getId, attempt.getDemandId())
                        .set(DemandEntity::getCurrentContactSessionId, null)
                        .set(DemandEntity::getCurrentOrderId, null)
        );
        Long nextAttemptId = dispatchNextAttempt(attempt.getDemandId());
        log.info("farmer rejected attemptId={} demandId={} nextAttemptId={}", attemptId, attempt.getDemandId(), nextAttemptId);
        return true;
    }

    @Override
    @Transactional
    public boolean ownerTimeout(Long attemptId) {
        MatchAttemptLite attempt = flowMapper.selectAttemptById(attemptId);
        if (attempt == null || !MatchAttemptStatus.PENDING_RESPONSE.name().equals(attempt.getStatus())) {
            log.warn("owner timeout skipped attemptId={} status={}", attemptId, attempt == null ? "null" : attempt.getStatus());
            return false;
        }
        flowMapper.closeAttempt(attemptId, MatchAttemptStatus.OWNER_TIMEOUT.name());
        flowMapper.updateDemandStatus(attempt.getDemandId(), DemandStatus.MATCHING.name());
        demandMapper.update(
                null,
                new LambdaUpdateWrapper<DemandEntity>()
                        .eq(DemandEntity::getId, attempt.getDemandId())
                        .set(DemandEntity::getCurrentContactSessionId, null)
                        .set(DemandEntity::getCurrentOrderId, null)
        );
        Long nextAttemptId = dispatchNextAttempt(attempt.getDemandId());
        log.info("owner timeout attemptId={} demandId={} nextAttemptId={}", attemptId, attempt.getDemandId(), nextAttemptId);
        return true;
    }

    @Override
    @Transactional
    public void handleOwnerTimeout() {
        List<Long> ids = flowMapper.selectOwnerTimeoutAttemptIds();
        if (ids == null) {
            return;
        }
        log.info("owner timeout sweep size={}", ids.size());
        for (Long attemptId : ids) {
            ownerTimeout(attemptId);
        }
    }

    @Override
    @Transactional
    public void handleFarmerTimeout() {
        List<Long> ids = flowMapper.selectFarmerTimeoutAttemptIds();
        if (ids == null) {
            return;
        }
        log.info("farmer timeout sweep size={}", ids.size());
        for (Long attemptId : ids) {
            MatchAttemptLite attempt = flowMapper.selectAttemptById(attemptId);
            if (attempt == null || !MatchAttemptStatus.WAIT_FARMER_CONFIRM.name().equals(attempt.getStatus())) {
                continue;
            }
            ContactSessionLite session = flowMapper.selectContactSessionByAttemptId(attemptId);
            if (session != null) {
                flowMapper.updateContactSessionStatus(session.getId(), ContactSessionStatus.CLOSED.name());
            }
            flowMapper.closeAttempt(attemptId, MatchAttemptStatus.FARMER_TIMEOUT.name());
            flowMapper.updateDemandStatus(attempt.getDemandId(), DemandStatus.MATCHING.name());
            demandMapper.update(
                    null,
                    new LambdaUpdateWrapper<DemandEntity>()
                            .eq(DemandEntity::getId, attempt.getDemandId())
                            .set(DemandEntity::getCurrentContactSessionId, null)
                            .set(DemandEntity::getCurrentOrderId, null)
            );
            Long nextAttemptId = dispatchNextAttempt(attempt.getDemandId());
            log.info("farmer timeout attemptId={} demandId={} nextAttemptId={}", attemptId, attempt.getDemandId(), nextAttemptId);
        }
    }

    private String describeTier(Integer tier) {
        if (tier == null) {
            return "UNSPECIFIED_TIER";
        }
        return switch (tier) {
            case 1 -> "NEAR_PRIORITY";
            case 2 -> "EXTENDED_CANDIDATE";
            case 3 -> "BACKUP_CANDIDATE";
            default -> "TIER_" + tier;
        };
    }
}
