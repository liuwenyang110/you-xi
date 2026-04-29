package com.nongzhushou.matchflow.service.impl;

import com.nongzhushou.matchflow.model.OwnerCandidate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service("matchflowMatchEngineBridgeService")
public class MatchEngineBridgeServiceImpl implements com.nongzhushou.matchflow.service.MatchEngineBridgeService {

    private final com.nongzhushou.match.service.MatchEngineBridgeService ruleBridgeService;
    private final Random random = new Random();

    public MatchEngineBridgeServiceImpl(com.nongzhushou.match.service.MatchEngineBridgeService ruleBridgeService) {
        this.ruleBridgeService = ruleBridgeService;
    }

    @Override
    public Map<Integer, List<OwnerCandidate>> buildTierPool(Long demandId) {
        Map<Integer, List<com.nongzhushou.match.model.OwnerCandidate>> source = ruleBridgeService.buildTierPool(demandId);
        Map<Integer, List<OwnerCandidate>> target = new LinkedHashMap<>();
        for (int tier = 1; tier <= 3; tier++) {
            List<OwnerCandidate> list = new ArrayList<>();
            for (com.nongzhushou.match.model.OwnerCandidate item : source.getOrDefault(tier, Collections.emptyList())) {
                OwnerCandidate candidate = new OwnerCandidate();
                candidate.setOwnerId(item.getOwnerId());
                candidate.setServiceItemId(item.getServiceItemId());
                candidate.setDistanceKm(item.getDistanceKm());
                candidate.setStraightDistanceKm(item.getStraightDistanceKm());
                candidate.setDurationMinutes(item.getDurationMinutes());
                candidate.setDistanceSource(item.getDistanceSource());
                candidate.setMatchScore(item.getScore());
                candidate.setReason(item.getReason());
                list.add(candidate);
            }
            target.put(tier, list);
        }
        return target;
    }

    @Override
    public OwnerCandidate randomPickFromTier(Map<Integer, List<OwnerCandidate>> tierPool, int tierNo) {
        List<OwnerCandidate> list = tierPool.getOrDefault(tierNo, Collections.emptyList());
        if (list.isEmpty()) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }
}
