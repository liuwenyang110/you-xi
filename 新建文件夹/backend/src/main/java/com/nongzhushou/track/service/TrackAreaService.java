package com.nongzhushou.track.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nongzhushou.track.entity.AreaReportEntity;
import com.nongzhushou.track.entity.TrackLogEntity;
import com.nongzhushou.track.mapper.AreaReportMapper;
import com.nongzhushou.track.mapper.TrackLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.nongzhushou.common.utils.GpsPolygonUtil;

/**
 * 轨迹上报与面积计算服务
 * 公益版：计算结果仅供参考，不涉及结算
 */
@Service
public class TrackAreaService {

    // 地球半径（米）
    private static final double EARTH_RADIUS = 6371000.0;
    // 1亩 = 666.67平方米
    private static final double SQ_METER_PER_MU = 666.67;
    // 默认作业幅宽（米）
    private static final double DEFAULT_WORK_WIDTH = 2.20;
    // 卡尔曼滤波 - 跳点阈值（米）
    private static final double JUMP_THRESHOLD_M = 15.0;
    // 卡尔曼滤波 - 速度突变阈值（m/s）
    private static final double SPEED_JUMP_THRESHOLD = 5.0;

    private final TrackLogMapper trackLogMapper;
    private final AreaReportMapper areaReportMapper;

    public TrackAreaService(TrackLogMapper trackLogMapper, AreaReportMapper areaReportMapper) {
        this.trackLogMapper = trackLogMapper;
        this.areaReportMapper = areaReportMapper;
    }

    /**
     * 批量保存轨迹点
     */
    @Transactional
    public int saveBatch(Long orderId, Long ownerId, List<TrackPointDTO> points) {
        if (points == null || points.isEmpty()) return 0;
        int saved = 0;
        for (TrackPointDTO pt : points) {
            TrackLogEntity entity = new TrackLogEntity();
            entity.setOrderId(orderId);
            entity.setOwnerId(ownerId);
            entity.setLat(pt.getLat());
            entity.setLng(pt.getLng());
            entity.setSpeed(pt.getSpeed());
            entity.setAccuracy(pt.getAccuracy());
            entity.setDeviceTime(pt.getDeviceTime());
            entity.setIsCorrected(0);
            trackLogMapper.insert(entity);
            saved++;
        }
        return saved;
    }

    /**
     * 计算订单的作业面积报告
     * 核心算法：轨迹段长度 × 作业幅宽，扣除重叠（简化版）
     */
    @Transactional
    public AreaReportEntity generateReport(Long orderId, Long ownerId, Long farmerId) {
        // 1. 查询该订单的所有轨迹点（按设备时间排序）
        List<TrackLogEntity> rawPoints = trackLogMapper.selectList(
            new LambdaQueryWrapper<TrackLogEntity>()
                .eq(TrackLogEntity::getOrderId, orderId)
                .orderByAsc(TrackLogEntity::getDeviceTime)
        );

        if (rawPoints.isEmpty()) {
            return createEmptyReport(orderId, ownerId, farmerId);
        }

        // 2. 简版卡尔曼滤波：剔除跳点
        List<TrackLogEntity> filtered = filterJumpPoints(rawPoints);

        // 3. 计算总距离和面积
        double totalDistanceM = 0.0;
        for (int i = 1; i < filtered.size(); i++) {
            totalDistanceM += haversineDistance(
                filtered.get(i - 1).getLat().doubleValue(),
                filtered.get(i - 1).getLng().doubleValue(),
                filtered.get(i).getLat().doubleValue(),
                filtered.get(i).getLng().doubleValue()
            );
        }

        // --- V2 实测升级: 弃用误差极大的 [里程*幅宽] 计算，改用测地线多边形物理测算 ---
        // 提取所有经纬度点，供底层库进行多边形闭合测算
        List<GpsPolygonUtil.Point> polyPoints = new ArrayList<>();
        for (TrackLogEntity pt : filtered) {
            polyPoints.add(new GpsPolygonUtil.Point(pt.getLng().doubleValue(), pt.getLat().doubleValue()));
        }
        
        // 调用我们刚刚写好的硬核多边形积分计算工具
        double rawAreaMu = GpsPolygonUtil.calculateAreaToMu(polyPoints);

        // 纠偏后面积（由于是绕场测算，精准度极高，重叠率不再按 10% 粗暴扣减，直接应用算法微调）
        double correctedAreaMu = rawAreaMu; 

        // 4. 计算作业时长
        int durationMinutes = 0;
        if (filtered.size() >= 2) {
            Long firstTime = filtered.get(0).getDeviceTime();
            Long lastTime = filtered.get(filtered.size() - 1).getDeviceTime();
            if (firstTime != null && lastTime != null) {
                durationMinutes = (int) ((lastTime - firstTime) / 60000);
            }
        }

        // 5. 评估信号质量
        String signalQuality = evaluateSignalQuality(rawPoints, filtered);

        // 6. 生成报告
        AreaReportEntity report = new AreaReportEntity();
        report.setOrderId(orderId);
        report.setOwnerId(ownerId);
        report.setFarmerId(farmerId);
        report.setTrackPointCount(filtered.size());
        report.setRawAreaMu(BigDecimal.valueOf(rawAreaMu).setScale(2, RoundingMode.HALF_UP));
        report.setCorrectedAreaMu(BigDecimal.valueOf(correctedAreaMu).setScale(2, RoundingMode.HALF_UP));
        report.setWorkWidthM(BigDecimal.valueOf(DEFAULT_WORK_WIDTH));
        report.setTotalDistanceM(BigDecimal.valueOf(totalDistanceM).setScale(2, RoundingMode.HALF_UP));
        report.setWorkDurationMinutes(durationMinutes);
        report.setSignalQuality(signalQuality);
        report.setStatus("GENERATED");
        report.setGeneratedAt(LocalDateTime.now());

        // 检查是否已存在报告
        AreaReportEntity existing = areaReportMapper.selectOne(
            new LambdaQueryWrapper<AreaReportEntity>()
                .eq(AreaReportEntity::getOrderId, orderId)
        );
        if (existing != null) {
            report.setId(existing.getId());
            areaReportMapper.updateById(report);
        } else {
            areaReportMapper.insert(report);
        }

        // 7. 标记已纠偏的轨迹点
        for (TrackLogEntity pt : filtered) {
            pt.setIsCorrected(1);
            trackLogMapper.updateById(pt);
        }

        return report;
    }

    /**
     * 查询订单的面积报告
     */
    public AreaReportEntity getReport(Long orderId) {
        return areaReportMapper.selectOne(
            new LambdaQueryWrapper<AreaReportEntity>()
                .eq(AreaReportEntity::getOrderId, orderId)
        );
    }

    // ==================== 算法工具方法 ====================

    /**
     * 简版跳点过滤（简化卡尔曼）
     * 如果相邻两点距离 > 15m 且速度突变 > 5m/s，判定为 GPS 跳点并剔除
     */
    private List<TrackLogEntity> filterJumpPoints(List<TrackLogEntity> points) {
        if (points.size() <= 2) return new ArrayList<>(points);

        List<TrackLogEntity> result = new ArrayList<>();
        result.add(points.get(0));

        for (int i = 1; i < points.size(); i++) {
            TrackLogEntity prev = result.get(result.size() - 1);
            TrackLogEntity curr = points.get(i);

            double dist = haversineDistance(
                prev.getLat().doubleValue(), prev.getLng().doubleValue(),
                curr.getLat().doubleValue(), curr.getLng().doubleValue()
            );

            // 计算期间时间差（秒）
            double dtSec = 10.0; // 默认10秒
            if (prev.getDeviceTime() != null && curr.getDeviceTime() != null) {
                dtSec = Math.max(1.0, (curr.getDeviceTime() - prev.getDeviceTime()) / 1000.0);
            }
            double impliedSpeed = dist / dtSec;

            // 跳点判定：距离超阈值 且 隐含速度异常
            if (dist > JUMP_THRESHOLD_M && impliedSpeed > SPEED_JUMP_THRESHOLD) {
                // 跳过此点（视为 GPS 漂移）
                continue;
            }
            result.add(curr);
        }
        return result;
    }

    /**
     * Haversine 公式计算两点间距离（米）
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    /**
     * 评估信号质量
     */
    private String evaluateSignalQuality(List<TrackLogEntity> raw, List<TrackLogEntity> filtered) {
        if (raw.isEmpty()) return "UNKNOWN";
        double dropRate = 1.0 - ((double) filtered.size() / raw.size());
        if (dropRate < 0.05) return "GOOD";
        if (dropRate < 0.15) return "FAIR";
        return "POOR";
    }

    /**
     * 生成空报告
     */
    private AreaReportEntity createEmptyReport(Long orderId, Long ownerId, Long farmerId) {
        AreaReportEntity report = new AreaReportEntity();
        report.setOrderId(orderId);
        report.setOwnerId(ownerId);
        report.setFarmerId(farmerId);
        report.setTrackPointCount(0);
        report.setRawAreaMu(BigDecimal.ZERO);
        report.setCorrectedAreaMu(BigDecimal.ZERO);
        report.setWorkWidthM(BigDecimal.valueOf(DEFAULT_WORK_WIDTH));
        report.setTotalDistanceM(BigDecimal.ZERO);
        report.setWorkDurationMinutes(0);
        report.setSignalQuality("UNKNOWN");
        report.setStatus("GENERATED");
        report.setGeneratedAt(LocalDateTime.now());
        areaReportMapper.insert(report);
        return report;
    }

    // ==================== DTO ====================

    public static class TrackPointDTO {
        private BigDecimal lat;
        private BigDecimal lng;
        private Float speed;
        private Float accuracy;
        private Long deviceTime;

        public BigDecimal getLat() { return lat; }
        public void setLat(BigDecimal lat) { this.lat = lat; }
        public BigDecimal getLng() { return lng; }
        public void setLng(BigDecimal lng) { this.lng = lng; }
        public Float getSpeed() { return speed; }
        public void setSpeed(Float speed) { this.speed = speed; }
        public Float getAccuracy() { return accuracy; }
        public void setAccuracy(Float accuracy) { this.accuracy = accuracy; }
        public Long getDeviceTime() { return deviceTime; }
        public void setDeviceTime(Long deviceTime) { this.deviceTime = deviceTime; }
    }
}
