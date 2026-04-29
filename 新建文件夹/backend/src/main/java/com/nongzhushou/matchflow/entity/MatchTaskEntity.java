package com.nongzhushou.matchflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("match_task")
public class MatchTaskEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long demandId;
    private Integer currentTier;
    private String status;
    private String failReason;
    private Long successOwnerId;
    private Long successServiceItemId;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
    public Integer getCurrentTier() { return currentTier; }
    public void setCurrentTier(Integer currentTier) { this.currentTier = currentTier; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }
    public Long getSuccessOwnerId() { return successOwnerId; }
    public void setSuccessOwnerId(Long successOwnerId) { this.successOwnerId = successOwnerId; }
    public Long getSuccessServiceItemId() { return successServiceItemId; }
    public void setSuccessServiceItemId(Long successServiceItemId) { this.successServiceItemId = successServiceItemId; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
