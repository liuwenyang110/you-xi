package com.nongzhushou.lifecycle.controller;

import com.nongzhushou.lifecycle.service.DemandLifecycleService;
import com.nongzhushou.recommend.service.RouteSuggestService;
import com.nongzhushou.v3user.entity.V3UserEntity;
import com.nongzhushou.v3user.service.V3UserService;
import com.nongzhushou.v3demand.mapper.V3DemandMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 需求生命周期 + 镇域雷达 + 顺路推荐 API
 */
@RestController
@RequestMapping("/api/v3/lifecycle")
public class LifecycleController {

    @Autowired private DemandLifecycleService lifecycleService;
    @Autowired private V3UserService v3UserService;
    @Autowired private V3DemandMapper demandMapper;
    @Autowired private RouteSuggestService routeSuggestService;

    /** 农户确认：活儿干完了 */
    @PostMapping("/demand/{demandId}/complete")
    public ResponseEntity<?> confirmComplete(@PathVariable Long demandId) {
        V3UserEntity user = v3UserService.getCurrentV3User();
        lifecycleService.confirmCompleted(demandId, user.getId());
        return ok("已确认完成，需求已归档");
    }

    /** 农户确认：还没完 */
    @PostMapping("/demand/{demandId}/ongoing")
    public ResponseEntity<?> confirmOngoing(@PathVariable Long demandId) {
        V3UserEntity user = v3UserService.getCurrentV3User();
        lifecycleService.confirmOngoing(demandId, user.getId());
        return ok("收到，继续帮您挂着");
    }

    /** 镇域雷达：按镇聚合各村的活跃需求数量 */
    @GetMapping("/radar")
    public ResponseEntity<?> radar(@RequestParam String townshipCode) {
        List<Map<String, Object>> bubbles = demandMapper.aggregateByTownship(townshipCode);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("townshipCode", townshipCode);
        result.put("villages", bubbles);
        return ok(result);
    }

    /** 顺路推荐：机手选定目标村后推荐沿途村的需求 */
    @GetMapping("/suggest")
    public ResponseEntity<?> suggestRoute(@RequestParam Long targetZoneId,
                                           @RequestParam Long myZoneId) {
        List<Map<String, Object>> suggestions = routeSuggestService.suggestRoute(targetZoneId, myZoneId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("suggestions", suggestions);
        return ok(result);
    }

    /** 跨镇悬赏检测：哪些镇运力紧缺 */
    @GetMapping("/bounty")
    public ResponseEntity<?> crossTownBounty() {
        List<Map<String, Object>> bounties = routeSuggestService.checkCrossTownBounty();
        return ok(bounties);
    }

    private ResponseEntity<?> ok(Object data) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("code", 200);
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }
}

