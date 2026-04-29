package com.nongzhushou.match.service;

import com.nongzhushou.match.model.OwnerCandidate;
import java.util.List;
import java.util.Map;

public interface MatchRuleService {
    List<OwnerCandidate> findEligibleCandidates(Long demandId);
    Map<Integer, List<OwnerCandidate>> buildTierPool(Long demandId);
}
