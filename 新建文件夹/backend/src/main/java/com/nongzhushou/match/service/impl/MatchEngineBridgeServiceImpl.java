package com.nongzhushou.match.service.impl;

import com.nongzhushou.match.model.GeoDistanceUtils;
import com.nongzhushou.match.model.OwnerCandidate;
import com.nongzhushou.match.service.MatchEngineBridgeService;
import com.nongzhushou.match.service.MatchRuleService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class MatchEngineBridgeServiceImpl implements MatchEngineBridgeService {

    private final MatchRuleService matchRuleService;
    private final Random random = new Random();

    public MatchEngineBridgeServiceImpl(MatchRuleService matchRuleService) {
        this.matchRuleService = matchRuleService;
    }

    @Override
    public Map<Integer, List<OwnerCandidate>> buildTierPool(Long demandId) {
        return matchRuleService.buildTierPool(demandId);
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
