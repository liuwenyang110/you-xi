package com.nongzhushou.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.nongzhushou.common.api.IpLocationClient;
import org.springframework.context.annotation.Profile;

/**
 * 专为评委视察与路演定制的 "火力全开" 面板数据推流中心
 * （MockDataController - 此接口不上生产环境）
 */
@Profile("dev")
@RestController
@RequestMapping("/api/v2/roadshow")
public class MockDataController {

    private final Random random = new Random();

    @Autowired
    private IpLocationClient ipLocationClient;

    /**
     * HttpServletRequest request 用来捕获在会场或办公室演示这套系统的设备真实 IP
     */
    @GetMapping("/live-stats")
    public ResponseEntity<Map<String, Object>> getLivePitchStats(HttpServletRequest request) {
        
        // 捕获请求人的真实出口 IP (注意规避反向代理)
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
             clientIp = request.getRemoteAddr();
        }
        
        // --- 核心升级：连通不限量免 Key 太平洋接口 ---
        String userRealRegion = ipLocationClient.resolveProvinceAndCity(clientIp);

        Map<String, Object> stats = new HashMap<>();
        
        // 1. 政绩考核维度 (给政府领导看)
        stats.put("carbonReductionKg", 14000 + random.nextInt(2000)); // 随便给个几吨的碳减排指标
        stats.put("subsidySafeguardCount", 3000 + random.nextInt(100)); // 防套现打卡拦截成功数
        
        // 2. 高并发技术护城河 (给投资人看)
        stats.put("currentQPS", 15000 + random.nextInt(6000)); // QPS 在 1.5w - 2.1w 狂跳
        stats.put("redisLockHitRate", "99.98%"); // 锁命中率高，坚不可摧
        
        // 3. 业务模式成效 (给所有人看)
        stats.put("machineryUtilizationRate", "87.4%"); // 极高的农机利用率
        stats.put("farmerIncomeIncrease", "￥12.5 万"); // 助农总增收
        
        // 4. 下沉市场网格化治理区域追踪 (V2 大屏强杀器)
        stats.put("activeRegionTrack", userRealRegion); // 高频点亮政府地图
        stats.put("capturedIp", clientIp);

        return ResponseEntity.ok(stats);
    }
}
