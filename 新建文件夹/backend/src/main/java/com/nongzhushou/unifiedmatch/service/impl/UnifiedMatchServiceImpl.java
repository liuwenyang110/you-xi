package com.nongzhushou.unifiedmatch.service.impl;

import com.nongzhushou.match.service.MatchRuleService;
import com.nongzhushou.matchflow.service.MatchFlowService;
import com.nongzhushou.unifiedmatch.model.OwnerCandidate;
import com.nongzhushou.unifiedmatch.service.UnifiedMatchService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class UnifiedMatchServiceImpl implements UnifiedMatchService {

    private final MatchRuleService matchRuleService;
    private final MatchFlowService matchFlowService;

    public UnifiedMatchServiceImpl(MatchRuleService matchRuleService, MatchFlowService matchFlowService) {
        this.matchRuleService = matchRuleService;
        this.matchFlowService = matchFlowService;
    }

    @Override
    public List<OwnerCandidate> findEligibleCandidates(Long demandId) {
        List<com.nongzhushou.match.model.OwnerCandidate> source = matchRuleService.findEligibleCandidates(demandId);
        List<OwnerCandidate> result = new ArrayList<>();
        for (com.nongzhushou.match.model.OwnerCandidate item : source) {
            result.add(convert(item));
        }
        return result;
    }

    @Override
    public Map<Integer, List<OwnerCandidate>> buildTierPool(Long demandId) {
        Map<Integer, List<com.nongzhushou.match.model.OwnerCandidate>> source = matchRuleService.buildTierPool(demandId);
        Map<Integer, List<OwnerCandidate>> result = new LinkedHashMap<>();
        for (int tier = 1; tier <= 3; tier++) {
            List<OwnerCandidate> converted = new ArrayList<>();
            for (com.nongzhushou.match.model.OwnerCandidate item : source.getOrDefault(tier, List.of())) {
                converted.add(convert(item));
            }
            result.put(tier, converted);
        }
        return result;
    }

    @Override
    public Long startMatchFlow(Long demandId) {
        return matchFlowService.startMatchFlow(demandId);
    }

    @Override
    public Long dispatchNextAttempt(Long demandId) {
        return matchFlowService.dispatchNextAttempt(demandId);
    }

    @Override
    public boolean ownerAccept(Long attemptId) {
        return matchFlowService.ownerAccept(attemptId);
    }

    @Override
    public boolean ownerReject(Long attemptId) {
        return matchFlowService.ownerReject(attemptId);
    }

    @Override
    public boolean farmerConfirm(Long attemptId) {
        return matchFlowService.farmerConfirm(attemptId);
    }

    @Override
    public boolean farmerReject(Long attemptId) {
        return matchFlowService.farmerReject(attemptId);
    }

    @Override
    public void handleOwnerTimeout() {
        matchFlowService.handleOwnerTimeout();
    }

    @Override
    public void handleFarmerTimeout() {
        matchFlowService.handleFarmerTimeout();
    }

    private OwnerCandidate convert(com.nongzhushou.match.model.OwnerCandidate item) {
        OwnerCandidate candidate = new OwnerCandidate();
        candidate.setOwnerId(item.getOwnerId());
        candidate.setServiceItemId(item.getServiceItemId());
        candidate.setDistanceKm(item.getDistanceKm());
        candidate.setStraightDistanceKm(item.getStraightDistanceKm());
        candidate.setDurationMinutes(item.getDurationMinutes());
        candidate.setDistanceSource(item.getDistanceSource());
        candidate.setMatchScore(item.getScore());
        candidate.setReason(item.getReason());
        return candidate;
    }
}
