package com.nongzhushou.serviceitem.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.serviceitem.dto.ServiceItemCreateRequest;
import com.nongzhushou.serviceitem.service.ServiceItemService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasRole('OWNER')")
@RestController
@Validated
@RequestMapping("/api/v1/service-items")
public class ServiceItemController {

    private final ServiceItemService serviceItemService;

    public ServiceItemController(ServiceItemService serviceItemService) {
        this.serviceItemService = serviceItemService;
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody ServiceItemCreateRequest request) {
        return Result.ok(serviceItemService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(
            @PathVariable @Positive(message = "id must be greater than 0") Long id,
            @Valid @RequestBody ServiceItemCreateRequest request
    ) {
        return Result.ok(serviceItemService.update(id, request));
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(serviceItemService.list());
    }
}
