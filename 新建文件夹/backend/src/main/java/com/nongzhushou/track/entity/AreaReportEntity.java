package com.nongzhushou.track.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("area_report")
public class AreaReportEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * @deprecated 旧商业订单关联 ID，公益平台重构中逐步迁移为 contactRevealLogId。
     *             新业务请使用 contactRevealLogId 关联联系记录。
     */
    @Deprecated
    private Long orderId;
    private Long ownerId;
    private Long farmerId;
    private Integer trackPointCount;
    private BigDecimal rawAreaMu;
    private BigDecimal correctedAreaMu;
    private BigDecimal workWidthM;
    private BigDecimal totalDistanceM;
    private Integer workDurationMinutes;
    private String signalQuality;
    private String status;
    private LocalDateTime generatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }
    public Integer getTrackPointCount() { return trackPointCount; }
    public void setTrackPointCount(Integer trackPointCount) { this.trackPointCount = trackPointCount; }
    public BigDecimal getRawAreaMu() { return rawAreaMu; }
    public void setRawAreaMu(BigDecimal rawAreaMu) { this.rawAreaMu = rawAreaMu; }
    public BigDecimal getCorrectedAreaMu() { return correctedAreaMu; }
    public void setCorrectedAreaMu(BigDecimal correctedAreaMu) { this.correctedAreaMu = correctedAreaMu; }
    public BigDecimal getWorkWidthM() { return workWidthM; }
    public void setWorkWidthM(BigDecimal workWidthM) { this.workWidthM = workWidthM; }
    public BigDecimal getTotalDistanceM() { return totalDistanceM; }
    public void setTotalDistanceM(BigDecimal totalDistanceM) { this.totalDistanceM = totalDistanceM; }
    public Integer getWorkDurationMinutes() { return workDurationMinutes; }
    public void setWorkDurationMinutes(Integer workDurationMinutes) { this.workDurationMinutes = workDurationMinutes; }
    public String getSignalQuality() { return signalQuality; }
    public void setSignalQuality(String signalQuality) { this.signalQuality = signalQuality; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
