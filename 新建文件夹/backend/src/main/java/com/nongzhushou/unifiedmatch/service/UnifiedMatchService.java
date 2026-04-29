package com.nongzhushou.unifiedmatch.service;

import com.nongzhushou.unifiedmatch.model.OwnerCandidate;
import java.util.List;
import java.util.Map;

public interface UnifiedMatchService {
    List<OwnerCandidate> findEligibleCandidates(Long demandId);
    Map<Integer, List<OwnerCandidate>> buildTierPool(Long demandId);
    Long startMatchFlow(Long demandId);
    Long dispatchNextAttempt(Long demandId);
    boolean ownerAccept(Long attemptId);
    boolean ownerReject(Long attemptId);
    boolean farmerConfirm(Long attemptId);
    boolean farmerReject(Long attemptId);
    void handleOwnerTimeout();
    void handleFarmerTimeout();
}
