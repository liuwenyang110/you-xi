package com.nongzhushou.demand.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.demand.dto.DemandCreateRequest;
import com.nongzhushou.demand.service.DemandService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasRole('FARMER')")
@RestController
@Validated
@RequestMapping("/api/v1/demands")
public class DemandController {

    private final DemandService demandService;

    public DemandController(DemandService demandService) {
        this.demandService = demandService;
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody DemandCreateRequest request) {
        return Result.ok(demandService.createDemand(request));
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(demandService.listDemands());
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable @Positive(message = "id must be greater than 0") Long id) {
        return Result.ok(demandService.detail(id));
    }

    @GetMapping("/{id}/match-status")
    public Result<Map<String, Object>> matchStatus(@PathVariable @Positive(message = "id must be greater than 0") Long id) {
        return Result.ok(demandService.matchStatus(id));
    }

    @PostMapping("/{id}/cancel")
    public Result<Map<String, Object>> cancel(@PathVariable @Positive(message = "id must be greater than 0") Long id) {
        return Result.ok(demandService.cancelDemand(id));
    }
}
