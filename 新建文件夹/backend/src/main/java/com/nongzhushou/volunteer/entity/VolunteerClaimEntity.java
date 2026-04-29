package com.nongzhushou.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("volunteer_claim")
public class VolunteerClaimEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long demandId;
    private Long postId;
    private Long volunteerId;
    private Long equipmentId;
    private String status;          // CLAIMED / EN_ROUTE / WORKING / FINISHED / CANCELLED
    private LocalDateTime claimedAt;
    private LocalDateTime arrivedAt;
    private LocalDateTime finishedAt;
    private Long areaReportId;
    private String farmerFeedback;
    private Integer farmerRating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getVolunteerId() { return volunteerId; }
    public void setVolunteerId(Long volunteerId) { this.volunteerId = volunteerId; }
    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getClaimedAt() { return claimedAt; }
    public void setClaimedAt(LocalDateTime claimedAt) { this.claimedAt = claimedAt; }
    public LocalDateTime getArrivedAt() { return arrivedAt; }
    public void setArrivedAt(LocalDateTime arrivedAt) { this.arrivedAt = arrivedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
    public Long getAreaReportId() { return areaReportId; }
    public void setAreaReportId(Long areaReportId) { this.areaReportId = areaReportId; }
    public String getFarmerFeedback() { return farmerFeedback; }
    public void setFarmerFeedback(String farmerFeedback) { this.farmerFeedback = farmerFeedback; }
    public Integer getFarmerRating() { return farmerRating; }
    public void setFarmerRating(Integer farmerRating) { this.farmerRating = farmerRating; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
