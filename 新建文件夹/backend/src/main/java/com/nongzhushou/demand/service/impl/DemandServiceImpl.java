package com.nongzhushou.demand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongzhushou.common.enums.ContactSessionStatus;
import com.nongzhushou.common.enums.DemandStatus;
import com.nongzhushou.common.enums.MatchAttemptStatus;
import com.nongzhushou.common.exception.BizException;
import com.nongzhushou.common.exception.ErrorCode;
import com.nongzhushou.common.security.AuthContext;
import com.nongzhushou.contact.entity.ContactSessionEntity;
import com.nongzhushou.contact.mapper.ContactSessionMapper;
import com.nongzhushou.demand.dto.DemandCreateRequest;
import com.nongzhushou.demand.entity.DemandEntity;
import com.nongzhushou.demand.mapper.DemandMapper;
import com.nongzhushou.demand.service.DemandService;
import com.nongzhushou.match.entity.MatchAttemptEntity;
import com.nongzhushou.match.mapper.MatchAttemptMapper;
import com.nongzhushou.matchflow.entity.MatchTaskEntity;
import com.nongzhushou.matchflow.mapper.MatchTaskMapper;
import com.nongzhushou.matchflow.service.MatchFlowService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DemandServiceImpl implements DemandService {

    private final DemandMapper demandMapper;
    private final MatchFlowService matchFlowService;
    private final ObjectMapper objectMapper;
    private final MatchAttemptMapper matchAttemptMapper;
    private final ContactSessionMapper contactSessionMapper;
    private final MatchTaskMapper matchTaskMapper;

    public DemandServiceImpl(
            DemandMapper demandMapper,
            MatchFlowService matchFlowService,
            ObjectMapper objectMapper,
            MatchAttemptMapper matchAttemptMapper,
            ContactSessionMapper contactSessionMapper,
            MatchTaskMapper matchTaskMapper
    ) {
        this.demandMapper = demandMapper;
        this.matchFlowService = matchFlowService;
        this.objectMapper = objectMapper;
        this.matchAttemptMapper = matchAttemptMapper;
        this.contactSessionMapper = contactSessionMapper;
        this.matchTaskMapper = matchTaskMapper;
    }

    @Override
    public Long createDemand(DemandCreateRequest request) {
        Long currentUserId = AuthContext.currentUserId();
        DemandEntity entity = new DemandEntity();
        entity.setFarmerId(currentUserId);
        entity.setServiceCategoryId(request.getServiceCategoryId());
        entity.setServiceSubcategoryId(request.getServiceSubcategoryId());
        entity.setCropCode(request.getCropCode());
        entity.setAreaMu(request.getAreaMu());
        entity.setScheduleType(request.getScheduleType());
        entity.setExpectedDate(request.getExpectedDate());
        entity.setVillageName(request.getVillageName());
        entity.setLat(request.getLat() == null ? null : BigDecimal.valueOf(request.getLat()));
        entity.setLng(request.getLng() == null ? null : BigDecimal.valueOf(request.getLng()));
        entity.setVoiceAssetId(request.getVoiceAssetId());
        entity.setVoiceText(request.getVoiceText());
        entity.setRemark(request.getRemark());
        entity.setRequirementJson(toJson(request.getRequirementJson()));
        entity.setGratitudeType(request.getGratitudeType());
        entity.setPlotTags(request.getPlotTags());
        entity.setPublishedAt(LocalDateTime.now());
        entity.setMatchAttemptCount(0);
        entity.setStatus(DemandStatus.PUBLISHED.name());
        demandMapper.insert(entity);

        Long attemptId = matchFlowService.startMatchFlow(entity.getId());
        demandMapper.update(
                null,
                new LambdaUpdateWrapper<DemandEntity>()
                        .eq(DemandEntity::getId, entity.getId())
                        .set(DemandEntity::getStatus, attemptId == null ? DemandStatus.MATCH_FAILED.name() : DemandStatus.MATCHING.name())
        );
        return entity.getId();
    }

    @Override
    public List<Map<String, Object>> listDemands() {
        Long currentUserId = AuthContext.currentUserId();
        return demandMapper.selectList(new LambdaQueryWrapper<DemandEntity>()
                        .eq(DemandEntity::getFarmerId, currentUserId)
                        .orderByDesc(DemandEntity::getId))
                .stream()
                .map(this::toMap)
                .toList();
    }

    @Override
    public Map<String, Object> detail(Long id) {
        DemandEntity entity = demandMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "demand not found");
        }
        if (!AuthContext.currentUserId().equals(entity.getFarmerId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "no permission to access demand");
        }
        return toMap(entity);
    }

    @Override
    public Map<String, Object> matchStatus(Long id) {
        DemandEntity entity = demandMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "demand not found");
        }
        if (!AuthContext.currentUserId().equals(entity.getFarmerId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "no permission to access demand");
        }
        MatchTaskEntity task = matchTaskMapper.selectOne(new LambdaQueryWrapper<MatchTaskEntity>()
                .eq(MatchTaskEntity::getDemandId, id)
                .orderByDesc(MatchTaskEntity::getId)
                .last("limit 1"));
        MatchAttemptEntity currentAttempt = entity.getCurrentMatchAttemptId() == null
                ? null
                : matchAttemptMapper.selectById(entity.getCurrentMatchAttemptId());
        List<Map<String, Object>> recentAttempts = matchAttemptMapper.selectList(
                        new LambdaQueryWrapper<MatchAttemptEntity>()
                                .eq(MatchAttemptEntity::getDemandId, id)
                                .orderByDesc(MatchAttemptEntity::getId)
                                .last("limit 5"))
                .stream()
                .map(this::toAttemptExplain)
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("demandId", id);
        result.put("exists", true);
        result.put("status", entity.getStatus());
        result.put("statusLabel", DemandStatus.labelOf(entity.getStatus()));
        result.put("statusDescription", DemandStatus.descriptionOf(entity.getStatus()));
        result.put("currentMatchAttemptId", entity.getCurrentMatchAttemptId());
        result.put("matchAttemptCount", entity.getMatchAttemptCount());
        result.put("currentContactSessionId", entity.getCurrentContactSessionId());
        result.put("currentOrderId", entity.getCurrentOrderId());
        result.put("matchTask", task == null ? null : toTaskExplain(task));
        result.put("currentAttempt", currentAttempt == null ? null : toAttemptExplain(currentAttempt));
        result.put("recentAttempts", recentAttempts);
        result.put("explain", buildDemandExplain(entity, task, currentAttempt));
        return result;
    }

    @Override
    public Map<String, Object> cancelDemand(Long id) {
        DemandEntity demand = demandMapper.selectById(id);
        if (demand == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "demand not found");
        }
        if (!AuthContext.currentUserId().equals(demand.getFarmerId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "no permission to cancel demand");
        }
        if (DemandStatus.CANCELLED.name().equals(demand.getStatus())) {
            return Map.of("id", id, "cancelled", true, "status", demand.getStatus());
        }
        if (!canCancel(demand.getStatus())) {
            throw new BizException(ErrorCode.INVALID_STATE, "current status does not allow cancel");
        }

        List<MatchAttemptEntity> attempts = matchAttemptMapper.selectList(
                new LambdaQueryWrapper<MatchAttemptEntity>()
                        .eq(MatchAttemptEntity::getDemandId, id)
                        .in(MatchAttemptEntity::getStatus,
                                MatchAttemptStatus.PENDING_RESPONSE.name(),
                                MatchAttemptStatus.WAIT_FARMER_CONFIRM.name())
        );
        for (MatchAttemptEntity attempt : attempts) {
            attempt.setStatus(MatchAttemptStatus.DEMAND_CANCELLED.name());
            attempt.setCloseReason(MatchAttemptStatus.DEMAND_CANCELLED.name());
            attempt.setHandledAt(LocalDateTime.now());
            matchAttemptMapper.updateById(attempt);
        }

        List<ContactSessionEntity> sessions = contactSessionMapper.selectList(
                new LambdaQueryWrapper<ContactSessionEntity>()
                        .eq(ContactSessionEntity::getDemandId, id)
                        .in(ContactSessionEntity::getStatus, "WAIT_FARMER_CONFIRM", "WAITING_FARMER_CONFIRM")
        );
        for (ContactSessionEntity session : sessions) {
            session.setStatus("CLOSED");
            session.setActiveFlag(0);
            contactSessionMapper.updateById(session);
        }

        MatchTaskEntity task = matchTaskMapper.selectOne(
                new LambdaQueryWrapper<MatchTaskEntity>()
                        .eq(MatchTaskEntity::getDemandId, id)
                        .last("limit 1")
        );
        if (task != null) {
            task.setStatus(DemandStatus.CANCELLED.name());
            task.setFailReason("DEMAND_CANCELLED");
            task.setEndedAt(LocalDateTime.now());
            matchTaskMapper.updateById(task);
        }

        demandMapper.update(
                null,
                new LambdaUpdateWrapper<DemandEntity>()
                        .eq(DemandEntity::getId, id)
                        .set(DemandEntity::getStatus, DemandStatus.CANCELLED.name())
                        .set(DemandEntity::getCurrentMatchAttemptId, null)
                        .set(DemandEntity::getCurrentContactSessionId, null)
                        .set(DemandEntity::getCurrentOrderId, null)
        );
        demand.setStatus(DemandStatus.CANCELLED.name());
        return Map.of("id", id, "cancelled", true, "status", demand.getStatus());
    }

    private Map<String, Object> toTaskExplain(MatchTaskEntity task) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", task.getId());
        map.put("status", task.getStatus());
        map.put("statusLabel", DemandStatus.labelOf(task.getStatus()));
        map.put("statusDescription", DemandStatus.descriptionOf(task.getStatus()));
        map.put("currentTier", task.getCurrentTier());
        map.put("failReason", task.getFailReason());
        map.put("failReasonText", describeTaskFailReason(task.getFailReason()));
        map.put("successOwnerId", task.getSuccessOwnerId());
        map.put("successServiceItemId", task.getSuccessServiceItemId());
        map.put("endedAt", task.getEndedAt());
        return map;
    }

    private Map<String, Object> toAttemptExplain(MatchAttemptEntity attempt) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", attempt.getId());
        map.put("status", attempt.getStatus());
        map.put("statusLabel", MatchAttemptStatus.labelOf(attempt.getStatus()));
        map.put("statusDescription", MatchAttemptStatus.descriptionOf(attempt.getStatus()));
        map.put("tierNo", attempt.getTierNo());
        map.put("distanceLayer", attempt.getDistanceLayer());
        map.put("distanceSource", attempt.getDistanceSource());
        map.put("distanceSourceText", describeDistanceSource(attempt.getDistanceSource()));
        map.put("routeDistanceKm", attempt.getRouteDistanceKm());
        map.put("straightDistanceKm", attempt.getStraightDistanceKm());
        map.put("etaMinutes", attempt.getEtaMinutes());
        map.put("matchReason", attempt.getMatchReason());
        map.put("closeReason", attempt.getCloseReason());
        map.put("ownerId", attempt.getOwnerId());
        map.put("serviceItemId", attempt.getServiceItemId());
        map.put("serviceName", attempt.getServiceName());
        map.put("handledAt", attempt.getHandledAt());
        return map;
    }

    private Map<String, Object> buildDemandExplain(
            DemandEntity demand,
            MatchTaskEntity task,
            MatchAttemptEntity currentAttempt
    ) {
        Map<String, Object> explain = new LinkedHashMap<>();
        explain.put("statusText", DemandStatus.labelOf(demand.getStatus()));
        explain.put("statusDescription", DemandStatus.descriptionOf(demand.getStatus()));
        explain.put("taskStatus", task == null ? null : task.getStatus());
        explain.put("taskStatusLabel", task == null ? null : DemandStatus.labelOf(task.getStatus()));
        explain.put("taskFailReason", task == null ? null : task.getFailReason());
        explain.put("taskFailReasonText", task == null ? null : describeTaskFailReason(task.getFailReason()));
        explain.put("currentAttemptStatus", currentAttempt == null ? null : currentAttempt.getStatus());
        explain.put("currentAttemptStatusLabel",
                currentAttempt == null ? null : MatchAttemptStatus.labelOf(currentAttempt.getStatus()));
        explain.put("currentAttemptReason", currentAttempt == null ? null : currentAttempt.getMatchReason());
        explain.put("currentAttemptDistanceSource", currentAttempt == null ? null : currentAttempt.getDistanceSource());
        explain.put("currentAttemptDistanceSourceText",
                currentAttempt == null ? null : describeDistanceSource(currentAttempt.getDistanceSource()));
        return explain;
    }

    private Map<String, Object> toMap(DemandEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("farmerId", entity.getFarmerId());
        map.put("serviceCategoryId", entity.getServiceCategoryId());
        map.put("serviceSubcategoryId", entity.getServiceSubcategoryId());
        map.put("cropCode", entity.getCropCode());
        map.put("areaMu", entity.getAreaMu());
        map.put("scheduleType", entity.getScheduleType());
        map.put("expectedDate", entity.getExpectedDate());
        map.put("villageName", entity.getVillageName());
        map.put("lat", entity.getLat());
        map.put("lng", entity.getLng());
        map.put("voiceAssetId", entity.getVoiceAssetId());
        map.put("voiceText", entity.getVoiceText());
        map.put("remark", entity.getRemark());
        map.put("requirementJson", entity.getRequirementJson());
        map.put("gratitudeType", entity.getGratitudeType());
        map.put("plotTags", entity.getPlotTags());
        map.put("status", entity.getStatus());
        map.put("statusLabel", DemandStatus.labelOf(entity.getStatus()));
        map.put("statusDescription", DemandStatus.descriptionOf(entity.getStatus()));
        map.put("publishedAt", entity.getPublishedAt());
        map.put("matchAttemptCount", entity.getMatchAttemptCount());
        map.put("currentMatchAttemptId", entity.getCurrentMatchAttemptId());
        map.put("currentContactSessionId", entity.getCurrentContactSessionId());
        map.put("currentOrderId", entity.getCurrentOrderId());
        return map;
    }

    private String toJson(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new BizException(ErrorCode.INVALID_REQUEST, "requirementJson serialize failed");
        }
    }

    private boolean canCancel(String status) {
        return DemandStatus.PUBLISHED.name().equals(status)
                || DemandStatus.MATCHING.name().equals(status)
                || DemandStatus.MATCH_FAILED.name().equals(status);
    }

    private String describeDemandStatus(String status) {
        if (status == null || status.isBlank()) {
            return "STATUS_UNKNOWN";
        }
        return switch (status) {
            case "PUBLISHED" -> "PUBLISHED_WAITING_MATCH";
            case "MATCHING" -> "MATCHING_IN_PROGRESS";
            case "WAIT_FARMER_CONTACT_CONFIRM" -> "WAITING_FARMER_CONTACT_CONFIRM";
            case "NEGOTIATING" -> "NEGOTIATING";
            case "MATCH_FAILED" -> "MATCH_FAILED_RETRY_ALLOWED";
            case "CANCELLED" -> "CANCELLED";
            default -> status;
        };
    }

    private String describeTaskFailReason(String failReason) {
        if (failReason == null || failReason.isBlank()) {
            return "NO_FAIL_REASON";
        }
        return switch (failReason) {
            case "NO_ELIGIBLE_OWNER" -> "NO_ELIGIBLE_OWNER";
            case "DEMAND_CANCELLED" -> "DEMAND_CANCELLED";
            default -> failReason;
        };
    }

    private String describeDistanceSource(String source) {
        if (source == null || source.isBlank()) {
            return "UNKNOWN_SOURCE";
        }
        return switch (source.toLowerCase()) {
            case "driving" -> "MAP_DRIVING_ROUTE";
            case "straight_line" -> "STRAIGHT_LINE_ESTIMATE";
            case "fallback", "fallback_geo", "fallback_cache", "manual_fallback" -> "LOCAL_FALLBACK_ESTIMATE";
            default -> source;
        };
    }
}
