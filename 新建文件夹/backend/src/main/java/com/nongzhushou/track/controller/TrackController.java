package com.nongzhushou.track.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.track.entity.AreaReportEntity;
import com.nongzhushou.track.service.TrackAreaService;
import com.nongzhushou.track.service.TrackAreaService.TrackPointDTO;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 轨迹上报与面积报告接口
 * 公益版：面积数据仅供参考，不涉及结算
 */
@RestController
@RequestMapping("/api/v1/track")
public class TrackController {

    private final TrackAreaService trackAreaService;

    public TrackController(TrackAreaService trackAreaService) {
        this.trackAreaService = trackAreaService;
    }

    /**
     * 批量上传轨迹点（机手端调用）
     * POST /api/v1/track/upload
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadTrack(@RequestBody TrackUploadRequest request) {
        if (request.getOrderId() == null) {
            return Result.fail("订单ID不能为空");
        }
        if (request.getOwnerId() == null) {
            return Result.fail("机手ID不能为空");
        }
        if (request.getPoints() == null || request.getPoints().isEmpty()) {
            return Result.fail("轨迹点不能为空");
        }

        int saved = trackAreaService.saveBatch(
            request.getOrderId(),
            request.getOwnerId(),
            request.getPoints()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("saved", saved);
        result.put("status", "ok");
        return Result.ok(result);
    }

    /**
     * 生成/刷新面积报告（作业完成时调用）
     * POST /api/v1/track/report/generate
     */
    @PostMapping("/report/generate")
    public Result<AreaReportEntity> generateReport(@RequestBody ReportGenerateRequest request) {
        if (request.getOrderId() == null) {
            return Result.fail("订单ID不能为空");
        }

        AreaReportEntity report = trackAreaService.generateReport(
            request.getOrderId(),
            request.getOwnerId(),
            request.getFarmerId()
        );
        return Result.ok(report);
    }

    /**
     * 查询面积报告（农户/机手均可）
     * GET /api/v1/track/report/{orderId}
     */
    @GetMapping("/report/{orderId}")
    public Result<AreaReportEntity> getReport(@PathVariable Long orderId) {
        AreaReportEntity report = trackAreaService.getReport(orderId);
        if (report == null) {
            return Result.fail("该订单暂无面积报告");
        }
        return Result.ok(report);
    }

    // ==================== Request DTOs ====================

    public static class TrackUploadRequest {
        private Long orderId;
        private Long ownerId;
        private List<TrackPointDTO> points;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public Long getOwnerId() { return ownerId; }
        public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
        public List<TrackPointDTO> getPoints() { return points; }
        public void setPoints(List<TrackPointDTO> points) { this.points = points; }
    }

    public static class ReportGenerateRequest {
        private Long orderId;
        private Long ownerId;
        private Long farmerId;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public Long getOwnerId() { return ownerId; }
        public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
        public Long getFarmerId() { return farmerId; }
        public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }
    }
}
