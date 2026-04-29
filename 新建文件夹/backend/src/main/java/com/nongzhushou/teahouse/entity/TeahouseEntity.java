package com.nongzhushou.teahouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("v3_zone_teahouse")
public class TeahouseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long zoneId;
    private String status;           // OPEN / CLOSING_WARN / EXTENDED / CLOSED
    private LocalDateTime lastDemandAt;
    private LocalDateTime warnSentAt;
    private Integer extendDays;
    private Long extendedBy;
    private LocalDateTime extendedAt;
    private LocalDateTime forceClosedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getLastDemandAt() { return lastDemandAt; }
    public void setLastDemandAt(LocalDateTime lastDemandAt) { this.lastDemandAt = lastDemandAt; }
    public LocalDateTime getWarnSentAt() { return warnSentAt; }
    public void setWarnSentAt(LocalDateTime warnSentAt) { this.warnSentAt = warnSentAt; }
    public Integer getExtendDays() { return extendDays; }
    public void setExtendDays(Integer extendDays) { this.extendDays = extendDays; }
    public Long getExtendedBy() { return extendedBy; }
    public void setExtendedBy(Long extendedBy) { this.extendedBy = extendedBy; }
    public LocalDateTime getExtendedAt() { return extendedAt; }
    public void setExtendedAt(LocalDateTime extendedAt) { this.extendedAt = extendedAt; }
    public LocalDateTime getForceClosedAt() { return forceClosedAt; }
    public void setForceClosedAt(LocalDateTime forceClosedAt) { this.forceClosedAt = forceClosedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
