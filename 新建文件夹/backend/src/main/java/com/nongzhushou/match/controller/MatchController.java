package com.nongzhushou.match.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.match.service.MatchService;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * @deprecated V10 联系平台转型后，主动匹配派单已非主要流程。
 * 该 API 为向后兼容保留，新糕请使用 ContactController#initiateContact。
 */
@Deprecated
@PreAuthorize("hasRole('OWNER')")
@RestController
@Validated
@RequestMapping("/api/v1/match-tasks")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(matchService.listTasks());
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable @Positive(message = "id must be greater than 0") Long id) {
        return Result.ok(matchService.getTask(id));
    }

    @PostMapping("/{id}/accept")
    public Result<Map<String, Object>> accept(@PathVariable @Positive(message = "id must be greater than 0") Long id) {
        return Result.ok(matchService.accept(id));
    }

    @PostMapping("/{id}/reject")
    public Result<Map<String, Object>> reject(@PathVariable @Positive(message = "id must be greater than 0") Long id) {
        return Result.ok(matchService.reject(id));
    }

    @PostMapping("/demand/{demandId}/retry")
    public Result<Map<String, Object>> retry(@PathVariable @Positive(message = "demandId must be greater than 0") Long demandId) {
        return Result.ok(matchService.retry(demandId));
    }
}
