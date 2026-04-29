package com.nongzhushou.zone.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.zone.entity.*;
import com.nongzhushou.zone.service.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v3")
public class ZoneController {

    @Autowired
    private ZoneService zoneService;

    /**
     * 获取省级列表
     * GET /api/v3/regions/provinces
     */
    @GetMapping("/regions/provinces")
    public Result<List<RegionEntity>> getProvinces() {
        return Result.ok(zoneService.getProvinces());
    }

    /**
     * 获取下级区划（市/县/乡镇）
     * GET /api/v3/regions?parentCode=410000000000
     */
    @GetMapping("/regions")
    public Result<List<RegionEntity>> getChildren(@RequestParam String parentCode) {
        return Result.ok(zoneService.getChildren(parentCode));
    }

    /**
     * 获取乡镇下的片区列表
     * GET /api/v3/zones?townshipCode=411722100000
     * 或按县查询：GET /api/v3/zones?countyCode=411722000000
     */
    @GetMapping("/zones")
    public Result<List<ZoneEntity>> getZones(
            @RequestParam(required = false) String townshipCode,
            @RequestParam(required = false) String countyCode) {
        List<ZoneEntity> zones;
        if (townshipCode != null) {
            zones = zoneService.getZonesByTownship(townshipCode);
        } else if (countyCode != null) {
            zones = zoneService.getZonesByCounty(countyCode);
        } else {
            return Result.fail("请传入 townshipCode 或 countyCode");
        }
        return Result.ok(zones);
    }

    /**
     * 片区详情（含农机数量）
     * GET /api/v3/zones/{zoneId}
     */
    @GetMapping("/zones/{zoneId}")
    public Result<ZoneEntity> getZoneDetail(@PathVariable Long zoneId) {
        ZoneEntity zone = zoneService.getZoneDetail(zoneId);
        if (zone == null) {
            return Result.fail("片区不存在");
        }
        return Result.ok(zone);
    }

    /**
     * 一次性获取所有字典数据（供前端缓存）
     * GET /api/v3/dicts
     * 返回：machineCategories / machineTypes / crops / workTypes
     */
    @GetMapping("/dicts")
    public Result<Map<String, Object>> getAllDicts() {
        return Result.ok(zoneService.getAllDicts());
    }

    /**
     * 获取农机大类
     * GET /api/v3/dicts/machine-categories
     */
    @GetMapping("/dicts/machine-categories")
    public Result<List<MachineCategoryEntity>> getMachineCategories() {
        return Result.ok(zoneService.getMachineCategories());
    }

    /**
     * 获取农机小类（可按大类过滤）
     * GET /api/v3/dicts/machine-types?categoryId=1
     */
    @GetMapping("/dicts/machine-types")
    public Result<List<MachineTypeEntity>> getMachineTypes(
            @RequestParam(required = false) Integer categoryId) {
        return Result.ok(zoneService.getMachineTypes(categoryId));
    }

    /**
     * 获取作物品种列表
     * GET /api/v3/dicts/crops
     */
    @GetMapping("/dicts/crops")
    public Result<List<CropEntity>> getCrops() {
        return Result.ok(zoneService.getCrops());
    }

    /**
     * 获取作业类型列表
     * GET /api/v3/dicts/work-types?categoryId=1
     */
    @GetMapping("/dicts/work-types")
    public Result<List<WorkTypeEntity>> getWorkTypes(
            @RequestParam(required = false) Integer categoryId) {
        return Result.ok(zoneService.getWorkTypes(categoryId));
    }

    // ===== 管理员接口（需要管理员权限，后续加鉴权注解） =====

    /**
     * 管理员创建片区
     * POST /api/v3/admin/zones
     */
    @PostMapping("/admin/zones")
    public Result<ZoneEntity> createZone(@RequestBody Map<String, Object> params) {
        return Result.ok(zoneService.createZone(params));
    }

    /**
     * 管理员切换片区状态
     * PUT /api/v3/admin/zones/{zoneId}/status
     */
    @PutMapping("/admin/zones/{zoneId}/status")
    public Result<Void> toggleZoneStatus(@PathVariable Long zoneId,
                                          @RequestParam String status) {
        zoneService.toggleZoneStatus(zoneId, status);
        return Result.ok(null);
    }

    /**
     * 查询县区下所有片区（含停用，管理员用）
     * GET /api/v3/admin/zones?countyCode=411722000000
     */
    @GetMapping("/admin/zones")
    public Result<List<ZoneEntity>> adminGetZones(@RequestParam String countyCode) {
        return Result.ok(zoneService.getAllZonesByCounty(countyCode));
    }
}
