package com.nongzhushou.match.service;

import com.nongzhushou.match.model.MatchRuleInput;
import com.nongzhushou.match.model.OwnerCandidate;
import java.util.List;

public interface MatchEligibilityService {
    List<OwnerCandidate> findEligibleCandidates(Long demandId);
    boolean isEligible(MatchRuleInput input);
}
