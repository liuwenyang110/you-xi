package com.nongzhushou.owner.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.match.service.MatchService;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

@PreAuthorize("hasRole('OWNER')")
@RestController
@Validated
@RequestMapping("/api/v1/dispatches")
public class DispatchController {

    private final MatchService matchService;

    public DispatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/pending")
    public Result<List<Map<String, Object>>> pending() {
        List<Map<String, Object>> pending = matchService.listTasks().stream()
                .filter(item -> "PENDING_RESPONSE".equals(item.get("status")))
                .collect(Collectors.toList());
        return Result.ok(pending);
    }

    @GetMapping("/{attemptId}")
    public Result<Map<String, Object>> detail(@PathVariable @Positive(message = "attemptId must be greater than 0") Long attemptId) {
        return Result.ok(matchService.getTask(attemptId));
    }

    @PostMapping("/{attemptId}/accept")
    public Result<Map<String, Object>> accept(@PathVariable @Positive(message = "attemptId must be greater than 0") Long attemptId) {
        return Result.ok(matchService.accept(attemptId));
    }

    @PostMapping("/{attemptId}/reject")
    public Result<Map<String, Object>> reject(@PathVariable @Positive(message = "attemptId must be greater than 0") Long attemptId) {
        return Result.ok(matchService.reject(attemptId));
    }
}
