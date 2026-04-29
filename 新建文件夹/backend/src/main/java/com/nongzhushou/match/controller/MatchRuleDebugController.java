package com.nongzhushou.match.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.match.model.OwnerCandidate;
import com.nongzhushou.match.service.MatchEngineBridgeService;
import com.nongzhushou.match.service.MatchRuleService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@RestController
@RequestMapping("/api/v1/debug/match")
public class MatchRuleDebugController {

    private final MatchRuleService matchRuleService;
    private final MatchEngineBridgeService matchEngineBridgeService;

    public MatchRuleDebugController(
            MatchRuleService matchRuleService,
            MatchEngineBridgeService matchEngineBridgeService
    ) {
        this.matchRuleService = matchRuleService;
        this.matchEngineBridgeService = matchEngineBridgeService;
    }

    @GetMapping("/{demandId}/eligible")
    public Result<List<OwnerCandidate>> eligible(@PathVariable Long demandId) {
        return Result.ok(matchRuleService.findEligibleCandidates(demandId));
    }

    @GetMapping("/{demandId}/tiers")
    public Result<Map<Integer, List<OwnerCandidate>>> tiers(@PathVariable Long demandId) {
        return Result.ok(matchEngineBridgeService.buildTierPool(demandId));
    }

    @GetMapping("/{demandId}/pick/{tierNo}")
    public Result<OwnerCandidate> pick(@PathVariable Long demandId, @PathVariable int tierNo) {
        Map<Integer, List<OwnerCandidate>> tierPool = matchEngineBridgeService.buildTierPool(demandId);
        return Result.ok(matchEngineBridgeService.randomPickFromTier(tierPool, tierNo));
    }
}
