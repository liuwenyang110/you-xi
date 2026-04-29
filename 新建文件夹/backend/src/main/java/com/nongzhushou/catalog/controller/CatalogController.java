package com.nongzhushou.catalog.controller;

import com.nongzhushou.catalog.service.CatalogService;
import com.nongzhushou.common.api.Result;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/service-categories")
    public Result<List<Map<String, Object>>> serviceCategories() {
        return Result.ok(catalogService.serviceCategories());
    }

    @GetMapping("/machine-types")
    public Result<List<Map<String, Object>>> machineTypes() {
        return Result.ok(catalogService.machineTypes());
    }

    @GetMapping("/demand-form/{subcategoryId}")
    public Result<List<Map<String, Object>>> demandForm(@PathVariable Long subcategoryId) {
        return Result.ok(catalogService.demandForm(subcategoryId));
    }

    @GetMapping("/equipment-form/{machineTypeId}")
    public Result<List<Map<String, Object>>> equipmentForm(@PathVariable Long machineTypeId) {
        return Result.ok(catalogService.equipmentForm(machineTypeId));
    }
}
