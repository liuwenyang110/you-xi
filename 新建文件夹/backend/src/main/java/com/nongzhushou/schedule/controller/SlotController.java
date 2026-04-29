package com.nongzhushou.schedule.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;

/**
 * 由后端大佬搭建 - 时空预约控制器 (槽位系统)
 * Spring Boot RESTful API 标准规范
 */
@RestController
@RequestMapping("/api/v2/schedule")
public class SlotController {

    /**
     * PM需要：获取某村落附近的可用农机槽位 (日历视图用)
     */
    @GetMapping("/slots/available")
    public ResponseEntity<Map<String, Object>> getAvailableSlots(
            @RequestParam String villageCode, 
            @RequestParam String date) {
        // Todo: 整合领域服务，查询 equipment_timetable
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "暂未对接算法底层，接口已打通");
        return ResponseEntity.ok(response);
    }

    /**
     * 用户点击抢订槽位
     */
    @PostMapping("/slots/lock")
    public ResponseEntity<Map<String, Object>> lockSlot(@RequestBody Map<String, Object> requestParams) {
        Long equipmentId = Long.valueOf(requestParams.get("equipmentId").toString());
        String slotDate = requestParams.get("date").toString();
        // 此处将调用第四位专家写的 Redis Lua 脚本进行分布式锁定
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "pending");
        response.put("message", "进入抢单队列处理中");
        return ResponseEntity.accepted().body(response);
    }
}
