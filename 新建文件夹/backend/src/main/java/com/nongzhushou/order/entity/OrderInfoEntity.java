package com.nongzhushou.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("order_info")
public class OrderInfoEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long demandId;
    private Long matchAttemptId;
    private Long contactSessionId;
    private Long ownerId;
    private Long farmerId;
    private Long serviceItemId;
    private String status;
    private Integer ownerConfirmedFinish;
    private Integer farmerConfirmedFinish;
    private LocalDateTime contactConfirmedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
    public Long getMatchAttemptId() { return matchAttemptId; }
    public void setMatchAttemptId(Long matchAttemptId) { this.matchAttemptId = matchAttemptId; }
    public Long getContactSessionId() { return contactSessionId; }
    public void setContactSessionId(Long contactSessionId) { this.contactSessionId = contactSessionId; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }
    public Long getServiceItemId() { return serviceItemId; }
    public void setServiceItemId(Long serviceItemId) { this.serviceItemId = serviceItemId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getOwnerConfirmedFinish() { return ownerConfirmedFinish; }
    public void setOwnerConfirmedFinish(Integer ownerConfirmedFinish) { this.ownerConfirmedFinish = ownerConfirmedFinish; }
    public Integer getFarmerConfirmedFinish() { return farmerConfirmedFinish; }
    public void setFarmerConfirmedFinish(Integer farmerConfirmedFinish) { this.farmerConfirmedFinish = farmerConfirmedFinish; }
    public LocalDateTime getContactConfirmedAt() { return contactConfirmedAt; }
    public void setContactConfirmedAt(LocalDateTime contactConfirmedAt) { this.contactConfirmedAt = contactConfirmedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
