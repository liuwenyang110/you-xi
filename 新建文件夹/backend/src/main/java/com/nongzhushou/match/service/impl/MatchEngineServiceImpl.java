package com.nongzhushou.match.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nongzhushou.common.enums.ApproveStatus;
import com.nongzhushou.common.enums.DemandStatus;
import com.nongzhushou.common.enums.MatchAttemptStatus;
import com.nongzhushou.common.enums.ServiceItemStatus;
import com.nongzhushou.common.util.RedisKeys;
import com.nongzhushou.contact.entity.ContactSessionEntity;
import com.nongzhushou.contact.mapper.ContactSessionMapper;
import com.nongzhushou.demand.entity.DemandEntity;
import com.nongzhushou.demand.mapper.DemandMapper;
import com.nongzhushou.match.entity.MatchAttemptEntity;
import com.nongzhushou.match.mapper.MatchAttemptMapper;
import com.nongzhushou.match.model.MatchRuleInput;
import com.nongzhushou.match.model.OwnerCandidate;
import com.nongzhushou.match.service.MatchConflictService;
import com.nongzhushou.match.service.MatchEngineService;
import com.nongzhushou.match.service.MatchEligibilityService;
import com.nongzhushou.equipment.entity.EquipmentEntity;
import com.nongzhushou.equipment.mapper.EquipmentMapper;
import com.nongzhushou.serviceitem.entity.ServiceItemEntity;
import com.nongzhushou.serviceitem.mapper.ServiceItemMapper;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class MatchEngineServiceImpl implements MatchEngineService {

    private static final List<Integer> TIER_DISTANCE_KM = List.of(3, 8, 15);

    private final StringRedisTemplate redisTemplate;
    private final DemandMapper demandMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final MatchAttemptMapper matchAttemptMapper;
    private final EquipmentMapper equipmentMapper;
    private final ContactSessionMapper contactSessionMapper;
    private final MatchConflictService matchConflictService;
    private final MatchEligibilityService matchEligibilityService;

    public MatchEngineServiceImpl(
            StringRedisTemplate redisTemplate,
            DemandMapper demandMapper,
            ServiceItemMapper serviceItemMapper,
            MatchAttemptMapper matchAttemptMapper,
            EquipmentMapper equipmentMapper,
            ContactSessionMapper contactSessionMapper,
            MatchConflictService matchConflictService,
            MatchEligibilityService matchEligibilityService
    ) {
        this.redisTemplate = redisTemplate;
        this.demandMapper = demandMapper;
        this.serviceItemMapper = serviceItemMapper;
        this.matchAttemptMapper = matchAttemptMapper;
        this.equipmentMapper = equipmentMapper;
        this.contactSessionMapper = contactSessionMapper;
        this.matchConflictService = matchConflictService;
        this.matchEligibilityService = matchEligibilityService;
    }

    @Override
    public MatchAttemptEntity tryDispatch(Long demandId) {
        DemandEntity demand = demandMapper.selectById(demandId);
        if (demand == null) {
            return null;
        }
        String lockKey = RedisKeys.matchDemandLock(demandId);
        boolean locked = false;
        try {
            Boolean lockResult = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(15));
            locked = Boolean.TRUE.equals(lockResult);
        } catch (Exception ignored) {
            locked = true;
        }
        if (!locked) {
            return null;
        }

        try {
            if (matchConflictService.hasActiveContactConflict(demandId) || hasActiveContact(demandId)) {
                demand.setStatus(DemandStatus.WAITING_FARMER_CONFIRM.name());
                demandMapper.updateById(demand);
                return null;
            }
            List<ServiceItemEntity> pool = serviceItemMapper.selectList(
                    new LambdaQueryWrapper<ServiceItemEntity>()
                            .eq(ServiceItemEntity::getServiceSubcategoryId, demand.getServiceSubcategoryId())
                            .eq(ServiceItemEntity::getStatus, ServiceItemStatus.ACTIVE.name())
                            .eq(ServiceItemEntity::getApproveStatus, ApproveStatus.PASSED.name())
                            .eq(ServiceItemEntity::getIsAcceptingOrders, true)
                            .orderByAsc(ServiceItemEntity::getId));
            if (pool.isEmpty()) {
                demand.setStatus(DemandStatus.MATCH_FAILED.name());
                demandMapper.updateById(demand);
                return null;
            }
            List<OwnerCandidate> eligibleCandidates = matchEligibilityService.findEligibleCandidates(demandId);
            if (eligibleCandidates.isEmpty()) {
                demand.setStatus(DemandStatus.MATCH_FAILED.name());
                demandMapper.updateById(demand);
                return null;
            }

            OwnerCandidate chosen = pickByTier(eligibleCandidates);
            ServiceItemEntity candidate = serviceItemMapper.selectById(chosen.getServiceItemId());

            MatchAttemptEntity attempt = new MatchAttemptEntity();
            attempt.setDemandId(demand.getId());
            attempt.setOwnerId(chosen.getOwnerId());
            attempt.setServiceItemId(chosen.getServiceItemId());
            attempt.setServiceName(candidate.getServiceName());
            attempt.setVillageName(demand.getVillageName());
            attempt.setDistanceLayer(resolveTierLabel(chosen.getDistanceKm()));
            attempt.setStatus(MatchAttemptStatus.PENDING_RESPONSE.name());
            attempt.setResponseDeadlineSeconds(120);
            matchAttemptMapper.insert(attempt);

            Integer oldCount = demand.getMatchAttemptCount() == null ? 0 : demand.getMatchAttemptCount();
            demand.setCurrentMatchAttemptId(attempt.getId());
            demand.setMatchAttemptCount(oldCount + 1);
            demand.setStatus(DemandStatus.MATCHING.name());
            demandMapper.updateById(demand);
            return attempt;
        } finally {
            try {
                redisTemplate.delete(lockKey);
            } catch (Exception ignored) {
            }
        }
    }

    private List<OwnerCandidate> deduplicateOwners(List<OwnerCandidate> candidates) {
        return candidates.stream()
                .collect(java.util.stream.Collectors.toMap(
                        OwnerCandidate::getOwnerId,
                        candidate -> candidate,
                        (left, right) -> left.getScore() >= right.getScore() ? left : right))
                .values()
                .stream()
                .sorted(Comparator.comparing(OwnerCandidate::getDistanceKm, Comparator.nullsLast(Double::compareTo))
                        .thenComparing(OwnerCandidate::getScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private OwnerCandidate pickByTier(List<OwnerCandidate> candidates) {
        for (int tier = 1; tier <= TIER_DISTANCE_KM.size(); tier++) {
            int currentTier = tier;
            List<OwnerCandidate> tierCandidates = candidates.stream()
                    .filter(item -> resolveTier(item.getDistanceKm()) == currentTier)
                    .sorted(Comparator.comparing(OwnerCandidate::getScore, Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(OwnerCandidate::getDistanceKm, Comparator.nullsLast(Double::compareTo)))
                    .toList();
            if (!tierCandidates.isEmpty()) {
                int bound = Math.min(3, tierCandidates.size());
                return tierCandidates.get(ThreadLocalRandom.current().nextInt(bound));
            }
        }
        throw new IllegalStateException("No eligible candidate found");
    }

    private int buildScore(Double distanceKm, boolean cropOk, boolean terrainOk, boolean plotOk) {
        int score = 100;
        if (cropOk) {
            score += 30;
        }
        if (terrainOk) {
            score += 20;
        }
        if (plotOk) {
            score += 10;
        }
        score -= (int) Math.round(distanceKm == null ? 999 : distanceKm);
        return score;
    }

    private int resolveTier(Double distanceKm) {
        if (distanceKm == null || distanceKm <= 3) {
            return 1;
        }
        if (distanceKm <= 8) {
            return 2;
        }
        return 3;
    }

    private String resolveTierLabel(Double distanceKm) {
        int tier = resolveTier(distanceKm);
        if (tier == 1) {
            return "0-3km";
        }
        if (tier == 2) {
            return "3-8km";
        }
        return "8-15km";
    }

    private boolean hasActiveContact(Long demandId) {
        return contactSessionMapper.selectCount(new LambdaQueryWrapper<ContactSessionEntity>()
                .eq(ContactSessionEntity::getDemandId, demandId)
                .in(ContactSessionEntity::getStatus, List.of("WAITING_FARMER_CONFIRM", "WAIT_FARMER_CONFIRM", "CONTACT_ACTIVE", "NEGOTIATING"))) > 0;
    }
}
