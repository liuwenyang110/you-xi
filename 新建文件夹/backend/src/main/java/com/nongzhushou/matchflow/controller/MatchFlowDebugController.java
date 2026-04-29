package com.nongzhushou.matchflow.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.matchflow.service.MatchFlowService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Profile;

/**
 * @deprecated V10 联系平台转型后，复杂匹配流程已弃用。
 * 介绍了 farmerConfirm/ownerAccept 的 ContactServiceImpl 直接联系模式不需要本流程。
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
@Profile("dev")
@RestController
@RequestMapping("/api/v1/debug/match-flow")
public class MatchFlowDebugController {

    private final MatchFlowService matchFlowService;

    public MatchFlowDebugController(MatchFlowService matchFlowService) {
        this.matchFlowService = matchFlowService;
    }

    @PostMapping("/{demandId}/start")
    public Result<Long> start(@PathVariable Long demandId) {
        return Result.ok(matchFlowService.startMatchFlow(demandId));
    }

    @PostMapping("/attempt/{attemptId}/owner-accept")
    public Result<Boolean> ownerAccept(@PathVariable Long attemptId) {
        return Result.ok(matchFlowService.ownerAccept(attemptId));
    }

    @PostMapping("/attempt/{attemptId}/owner-reject")
    public Result<Boolean> ownerReject(@PathVariable Long attemptId) {
        return Result.ok(matchFlowService.ownerReject(attemptId));
    }

    @PostMapping("/attempt/{attemptId}/farmer-confirm")
    public Result<Boolean> farmerConfirm(@PathVariable Long attemptId) {
        return Result.ok(matchFlowService.farmerConfirm(attemptId));
    }

    @PostMapping("/attempt/{attemptId}/farmer-reject")
    public Result<Boolean> farmerReject(@PathVariable Long attemptId) {
        return Result.ok(matchFlowService.farmerReject(attemptId));
    }

    @PostMapping("/timeout/owner")
    public Result<Boolean> handleOwnerTimeout() {
        matchFlowService.handleOwnerTimeout();
        return Result.ok(true);
    }

    @PostMapping("/timeout/farmer")
    public Result<Boolean> handleFarmerTimeout() {
        matchFlowService.handleFarmerTimeout();
        return Result.ok(true);
    }
}
