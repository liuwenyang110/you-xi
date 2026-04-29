package com.nongzhushou.equipment.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.equipment.dto.EquipmentCreateRequest;
import com.nongzhushou.equipment.service.EquipmentService;
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
@RequestMapping("/api/v1/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody EquipmentCreateRequest request) {
        return Result.ok(equipmentService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(
            @PathVariable @Positive(message = "id must be greater than 0") Long id,
            @Valid @RequestBody EquipmentCreateRequest request
    ) {
        return Result.ok(equipmentService.update(id, request));
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(equipmentService.list());
    }
}
