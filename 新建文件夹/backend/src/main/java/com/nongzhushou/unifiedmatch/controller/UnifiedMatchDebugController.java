package com.nongzhushou.unifiedmatch.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.unifiedmatch.model.OwnerCandidate;
import com.nongzhushou.unifiedmatch.service.UnifiedMatchService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Profile;

/**
 * @deprecated V10 联系平台转型后，复杂匹配算法小组已弃用。
 * 简单筛选替代智能匹配，本 Controller 仅作孟右备用和历史对标用途。
 * 请使用 ContactController#initiateContact 发起直接联系。
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
@Profile("dev")
@RestController
@RequestMapping("/api/v1/debug/unified-match")
public class UnifiedMatchDebugController {

    private final UnifiedMatchService service;

    public UnifiedMatchDebugController(UnifiedMatchService service) {
        this.service = service;
    }

    @GetMapping("/{demandId}/eligible")
    public Result<List<OwnerCandidate>> eligible(@PathVariable Long demandId) {
        return Result.ok(service.findEligibleCandidates(demandId));
    }

    @GetMapping("/{demandId}/tiers")
    public Result<Map<Integer, List<OwnerCandidate>>> tiers(@PathVariable Long demandId) {
        return Result.ok(service.buildTierPool(demandId));
    }

    @PostMapping("/{demandId}/start")
    public Result<Long> start(@PathVariable Long demandId) {
        return Result.ok(service.startMatchFlow(demandId));
    }

    @PostMapping("/attempt/{attemptId}/owner-accept")
    public Result<Boolean> ownerAccept(@PathVariable Long attemptId) {
        return Result.ok(service.ownerAccept(attemptId));
    }

    @PostMapping("/attempt/{attemptId}/owner-reject")
    public Result<Boolean> ownerReject(@PathVariable Long attemptId) {
        return Result.ok(service.ownerReject(attemptId));
    }

    @PostMapping("/attempt/{attemptId}/farmer-confirm")
    public Result<Boolean> farmerConfirm(@PathVariable Long attemptId) {
        return Result.ok(service.farmerConfirm(attemptId));
    }

    @PostMapping("/attempt/{attemptId}/farmer-reject")
    public Result<Boolean> farmerReject(@PathVariable Long attemptId) {
        return Result.ok(service.farmerReject(attemptId));
    }
}
