package com.nongzhushou.v3machinery.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.v3machinery.dto.V3MachineryRequest;
import com.nongzhushou.v3machinery.entity.V3MachineryEntity;
import com.nongzhushou.v3machinery.service.V3MachineryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v3")
public class V3MachineryController {

    @Autowired
    private V3MachineryService machineryService;

    /**
     * 农机手登记农机
     * POST /api/v3/machinery
     */
    @PostMapping("/machinery")
    public Result<V3MachineryEntity> register(@Valid @RequestBody V3MachineryRequest req) {
        return Result.ok(machineryService.register(req));
    }

    /**
     * 更新农机信息
     * PUT /api/v3/machinery/{id}
     */
    @PutMapping("/machinery/{id}")
    public Result<V3MachineryEntity> update(@PathVariable Long id,
                                             @Valid @RequestBody V3MachineryRequest req) {
        return Result.ok(machineryService.update(id, req));
    }

    /**
     * 下架农机
     * DELETE /api/v3/machinery/{id}
     */
    @DeleteMapping("/machinery/{id}")
    public Result<Void> deactivate(@PathVariable Long id) {
        machineryService.deactivate(id);
        return Result.ok(null);
    }

    /**
     * 我的农机列表（农机手）
     * GET /api/v3/machinery/my
     */
    @GetMapping("/machinery/my")
    public Result<List<V3MachineryEntity>> myMachinery() {
        return Result.ok(machineryService.myMachinery());
    }

    /**
     * 片区农机列表（本片区农户可见，含虚拟联系方式入口）
     * GET /api/v3/zones/{zoneId}/machinery
     */
    @GetMapping("/zones/{zoneId}/machinery")
    public Result<List<V3MachineryEntity>> getZoneMachinery(@PathVariable Long zoneId) {
        return Result.ok(machineryService.getZoneMachinery(zoneId));
    }

    /**
     * 片区农机分类统计（公开）
     * GET /api/v3/zones/{zoneId}/machinery/stats
     * 返回：[{categoryName:"收获机械", icon:"🌾", cnt:3}, ...]
     */
    @GetMapping("/zones/{zoneId}/machinery/stats")
    public Result<List<Map<String, Object>>> getZoneStats(@PathVariable Long zoneId) {
        return Result.ok(machineryService.getZoneStats(zoneId));
    }
}
