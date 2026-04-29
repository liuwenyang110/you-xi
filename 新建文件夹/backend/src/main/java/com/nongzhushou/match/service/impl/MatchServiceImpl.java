package com.nongzhushou.match.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nongzhushou.common.enums.ContactSessionStatus;
import com.nongzhushou.common.enums.DemandStatus;
import com.nongzhushou.common.enums.MatchAttemptStatus;
import com.nongzhushou.common.exception.BizException;
import com.nongzhushou.common.exception.ErrorCode;
import com.nongzhushou.common.security.AuthContext;
import com.nongzhushou.contact.entity.ContactSessionEntity;
import com.nongzhushou.contact.mapper.ContactSessionMapper;
import com.nongzhushou.demand.entity.DemandEntity;
import com.nongzhushou.demand.mapper.DemandMapper;
import com.nongzhushou.match.entity.MatchAttemptEntity;
import com.nongzhushou.match.mapper.MatchAttemptMapper;
import com.nongzhushou.match.service.MatchEngineService;
import com.nongzhushou.match.service.MatchService;
import com.nongzhushou.matchflow.service.MatchFlowService;
import com.nongzhushou.order.mapper.OrderInfoMapper;
import com.nongzhushou.serviceitem.entity.ServiceItemEntity;
import com.nongzhushou.serviceitem.mapper.ServiceItemMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MatchServiceImpl implements MatchService {

    private final MatchAttemptMapper matchAttemptMapper;
    private final DemandMapper demandMapper;
    private final ContactSessionMapper contactSessionMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final MatchEngineService matchEngineService;
    private final MatchFlowService matchFlowService;
    private final ServiceItemMapper serviceItemMapper;

    public MatchServiceImpl(
            MatchAttemptMapper matchAttemptMapper,
            DemandMapper demandMapper,
            ContactSessionMapper contactSessionMapper,
            OrderInfoMapper orderInfoMapper,
            MatchEngineService matchEngineService,
            MatchFlowService matchFlowService,
            ServiceItemMapper serviceItemMapper
    ) {
        this.matchAttemptMapper = matchAttemptMapper;
        this.demandMapper = demandMapper;
        this.contactSessionMapper = contactSessionMapper;
        this.orderInfoMapper = orderInfoMapper;
        this.matchEngineService = matchEngineService;
        this.matchFlowService = matchFlowService;
        this.serviceItemMapper = serviceItemMapper;
    }

    @Override
    public List<Map<String, Object>> listTasks() {
        Long ownerId = AuthContext.currentUserId();
        return matchAttemptMapper.selectList(new LambdaQueryWrapper<MatchAttemptEntity>()
                        .eq(MatchAttemptEntity::getOwnerId, ownerId)
                        .orderByDesc(MatchAttemptEntity::getId))
                .stream()
                .map(this::toMap)
                .toList();
    }

    @Override
    public Map<String, Object> getTask(Long id) {
        MatchAttemptEntity entity = matchAttemptMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "match attempt not found");
        }
        ensureOwnerAccess(entity);
        return toMap(entity);
    }

    @Override
    public Map<String, Object> accept(Long id) {
        MatchAttemptEntity match = matchAttemptMapper.selectById(id);
        if (match == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "match attempt not found");
        }
        ensureOwnerAccess(match);
        if (!matchFlowService.ownerAccept(id)) {
            throw new BizException(ErrorCode.INVALID_STATE, "current attempt does not allow accept");
        }
        match = matchAttemptMapper.selectById(id);
        ContactSessionEntity contact = contactSessionMapper.selectOne(new LambdaQueryWrapper<ContactSessionEntity>()
                .eq(ContactSessionEntity::getMatchAttemptId, id)
                .last("limit 1"));
        DemandEntity demand = demandMapper.selectById(match.getDemandId());
        if (demand != null && contact != null) {
            demand.setCurrentMatchAttemptId(match.getId());
            demand.setCurrentContactSessionId(contact.getId());
            demandMapper.updateById(demand);
        }
        return Map.of("matchAttempt", toMap(match), "contactSession", contact == null ? null : toMap(contact));
    }

    @Override
    public Map<String, Object> reject(Long id) {
        MatchAttemptEntity match = matchAttemptMapper.selectById(id);
        if (match == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "match attempt not found");
        }
        ensureOwnerAccess(match);
        if (!matchFlowService.ownerReject(id)) {
            throw new BizException(ErrorCode.INVALID_STATE, "current attempt does not allow reject");
        }
        MatchAttemptEntity nextPending = matchAttemptMapper.selectOne(new LambdaQueryWrapper<MatchAttemptEntity>()
                .eq(MatchAttemptEntity::getDemandId, match.getDemandId())
                .eq(MatchAttemptEntity::getOwnerId, match.getOwnerId())
                .eq(MatchAttemptEntity::getStatus, MatchAttemptStatus.PENDING_RESPONSE.name())
                .orderByDesc(MatchAttemptEntity::getId)
                .last("limit 1"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("matchAttempt", toMap(match));
        result.put("nextMatchAttempt", nextPending == null ? null : toMap(nextPending));
        return result;
    }

    @Override
    public Map<String, Object> retry(Long demandId) {
        DemandEntity demand = demandMapper.selectById(demandId);
        if (demand == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "demand not found");
        }
        Long ownerId = AuthContext.currentUserId();
        MatchAttemptEntity latestAttempt = matchAttemptMapper.selectOne(new LambdaQueryWrapper<MatchAttemptEntity>()
                .eq(MatchAttemptEntity::getDemandId, demandId)
                .orderByDesc(MatchAttemptEntity::getId)
                .last("limit 1"));
        if (latestAttempt != null && !ownerId.equals(latestAttempt.getOwnerId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "no permission to retry demand");
        }
        MatchAttemptEntity pending = matchAttemptMapper.selectOne(new LambdaQueryWrapper<MatchAttemptEntity>()
                .eq(MatchAttemptEntity::getDemandId, demandId)
                .eq(MatchAttemptEntity::getOwnerId, ownerId)
                .eq(MatchAttemptEntity::getStatus, MatchAttemptStatus.PENDING_RESPONSE.name())
                .last("limit 1"));
        if (pending != null) {
            return Map.of("demandId", demandId, "matched", true, "currentMatchAttempt", toMap(pending));
        }
        Long nextAttemptId = matchFlowService.dispatchNextAttempt(demandId);
        if (nextAttemptId == null) {
            demandMapper.update(
                    null,
                    new LambdaUpdateWrapper<DemandEntity>()
                            .eq(DemandEntity::getId, demandId)
                            .set(DemandEntity::getStatus, DemandStatus.MATCH_FAILED.name())
                            .set(DemandEntity::getCurrentMatchAttemptId, null)
                            .set(DemandEntity::getCurrentContactSessionId, null)
            );
            return Map.of("demandId", demandId, "matched", false, "message", "No more available owners");
        }
        MatchAttemptEntity nextAttempt = matchAttemptMapper.selectById(nextAttemptId);
        return Map.of("demandId", demandId, "matched", true, "currentMatchAttempt", toMap(nextAttempt));
    }

    private Map<String, Object> toMap(MatchAttemptEntity entity) {
        DemandEntity demand = entity.getDemandId() == null ? null : demandMapper.selectById(entity.getDemandId());
        ServiceItemEntity serviceItem = entity.getServiceItemId() == null ? null : serviceItemMapper.selectById(entity.getServiceItemId());
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("demandId", entity.getDemandId());
        map.put("ownerId", entity.getOwnerId());
        map.put("serviceItemId", entity.getServiceItemId());
        map.put("serviceName", normalizeText(entity.getServiceName(), serviceItem == null ? "PENDING_SERVICE" : serviceItem.getServiceName()));
        map.put("villageName", normalizeVillageName(entity.getVillageName(), demand == null ? "LOCATION" : demand.getVillageName()));
        map.put("distanceLayer", normalizeDistanceLayer(entity.getDistanceLayer(), entity.getTierNo()));
        map.put("distanceSource", entity.getDistanceSource());
        map.put("routeDistanceKm", entity.getRouteDistanceKm());
        map.put("straightDistanceKm", entity.getStraightDistanceKm());
        map.put("etaMinutes", entity.getEtaMinutes());
        map.put("matchReason", entity.getMatchReason());
        map.put("closeReason", entity.getCloseReason());
        map.put("status", entity.getStatus());
        map.put("statusLabel", MatchAttemptStatus.labelOf(entity.getStatus()));
        map.put("responseDeadlineSeconds", entity.getResponseDeadlineSeconds());
        map.put("handledAt", entity.getHandledAt());
        map.put("explain", buildAttemptExplain(entity));
        return map;
    }

    private Map<String, Object> toMap(ContactSessionEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("demandId", entity.getDemandId());
        map.put("matchAttemptId", entity.getMatchAttemptId());
        map.put("ownerId", entity.getOwnerId());
        map.put("farmerId", entity.getFarmerId());
        map.put("status", entity.getStatus());
        map.put("statusLabel", ContactSessionStatus.labelOf(entity.getStatus()));
        map.put("activeFlag", entity.getActiveFlag());
        map.put("maskedPhone", entity.getMaskedPhone());
        return map;
    }

    private String normalizeText(String value, String fallback) {
        if (value == null || value.isBlank() || "???".equals(value)) {
            return fallback;
        }
        return value;
    }

    private String normalizeVillageName(String value, String fallback) {
        return normalizeText(value, fallback == null || fallback.isBlank() || "???".equals(fallback) ? "LOCATION" : fallback);
    }

    private String normalizeDistanceLayer(String value, Integer tier) {
        if (value == null || value.isBlank() || value.contains("?")) {
            return describeTier(tier);
        }
        return value;
    }

    private Map<String, Object> buildAttemptExplain(MatchAttemptEntity entity) {
        Map<String, Object> explain = new LinkedHashMap<>();
        explain.put("statusSummary", MatchAttemptStatus.labelOf(entity.getStatus()));
        explain.put("statusDescription", MatchAttemptStatus.descriptionOf(entity.getStatus()));
        explain.put("statusDetail", describeAttemptStatus(entity.getStatus(), entity.getCloseReason()));
        explain.put("distanceLayer", describeTier(entity.getTierNo()));
        explain.put("distanceSource", describeDistanceSource(entity.getDistanceSource()));
        explain.put("distanceSourceCode", entity.getDistanceSource());
        explain.put("routeDistanceKm", entity.getRouteDistanceKm());
        explain.put("straightDistanceKm", entity.getStraightDistanceKm());
        explain.put("etaMinutes", entity.getEtaMinutes());
        explain.put("matchReason", entity.getMatchReason());
        explain.put("closeReason", entity.getCloseReason());
        explain.put("closeReasonText", describeCloseReason(entity.getCloseReason()));
        return explain;
    }

    private void ensureOwnerAccess(MatchAttemptEntity entity) {
        Long ownerId = AuthContext.currentUserId();
        if (!ownerId.equals(entity.getOwnerId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "no permission to access match attempt");
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

    private String describeDistanceSource(String source) {
        if (source == null || source.isBlank()) {
            return "UNKNOWN_SOURCE";
        }
        return switch (source.toLowerCase()) {
            case "driving" -> "MAP_DRIVING_ROUTE";
            case "straight_line" -> "STRAIGHT_LINE_ESTIMATE";
            case "fallback", "fallback_geo", "fallback_cache", "manual_fallback" -> "LOCAL_FALLBACK_ESTIMATE";
            default -> "SOURCE_" + source;
        };
    }

    private String describeAttemptStatus(String status, String closeReason) {
        if (status == null || status.isBlank()) {
            return "STATUS_UNKNOWN";
        }
        return switch (status) {
            case "PENDING_RESPONSE" -> "DISPATCHED_WAITING_OWNER_RESPONSE";
            case "WAIT_FARMER_CONFIRM" -> "OWNER_ACCEPTED_WAITING_FARMER_CONFIRM";
            case "CONTACT_OPENED" -> "CONTACT_ESTABLISHED";
            case "OWNER_REJECTED" -> "OWNER_REJECTED_REDISPATCHING";
            case "FARMER_REJECTED" -> "FARMER_REJECTED_REDISPATCHING";
            case "OWNER_TIMEOUT" -> "OWNER_TIMEOUT_REDISPATCHING";
            case "FARMER_TIMEOUT" -> "FARMER_TIMEOUT_REDISPATCHING";
            case "DEMAND_CANCELLED" -> "DEMAND_CANCELLED";
            default -> "STATUS_" + status + (closeReason == null ? "" : "_CLOSE_" + closeReason);
        };
    }

    private String describeCloseReason(String closeReason) {
        if (closeReason == null || closeReason.isBlank()) {
            return "NOT_CLOSED";
        }
        return switch (closeReason) {
            case "OWNER_REJECTED" -> "OWNER_REJECTED";
            case "FARMER_REJECTED" -> "FARMER_REJECTED";
            case "OWNER_TIMEOUT" -> "OWNER_TIMEOUT";
            case "FARMER_TIMEOUT" -> "FARMER_TIMEOUT";
            case "DEMAND_CANCELLED" -> "DEMAND_CANCELLED";
            default -> closeReason;
        };
    }
}
