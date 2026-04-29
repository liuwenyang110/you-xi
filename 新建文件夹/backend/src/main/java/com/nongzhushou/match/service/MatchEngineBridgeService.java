package com.nongzhushou.match.service;

import com.nongzhushou.match.model.OwnerCandidate;
import java.util.List;
import java.util.Map;

public interface MatchEngineBridgeService {
    Map<Integer, List<OwnerCandidate>> buildTierPool(Long demandId);
    OwnerCandidate randomPickFromTier(Map<Integer, List<OwnerCandidate>> tierPool, int tierNo);
}
