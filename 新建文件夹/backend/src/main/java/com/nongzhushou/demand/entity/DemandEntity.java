package com.nongzhushou.demand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("demand")
public class DemandEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long farmerId;
    private Long serviceCategoryId;
    private Long serviceSubcategoryId;
    private String cropCode;
    private BigDecimal areaMu;
    private String scheduleType;
    private LocalDate expectedDate;
    private String villageName;
    private BigDecimal lat;
    private BigDecimal lng;
    private Long voiceAssetId;
    private String voiceText;
    private String remark;
    private String requirementJson;
    private String gratitudeType;
    private String plotTags;
    private String status;
    private LocalDateTime publishedAt;
    private Integer matchAttemptCount;
    private Long currentMatchAttemptId;
    private Long currentContactSessionId;
    private Long currentOrderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }
    public Long getServiceCategoryId() { return serviceCategoryId; }
    public void setServiceCategoryId(Long serviceCategoryId) { this.serviceCategoryId = serviceCategoryId; }
    public Long getServiceSubcategoryId() { return serviceSubcategoryId; }
    public void setServiceSubcategoryId(Long serviceSubcategoryId) { this.serviceSubcategoryId = serviceSubcategoryId; }
    public String getCropCode() { return cropCode; }
    public void setCropCode(String cropCode) { this.cropCode = cropCode; }
    public BigDecimal getAreaMu() { return areaMu; }
    public void setAreaMu(BigDecimal areaMu) { this.areaMu = areaMu; }
    public String getScheduleType() { return scheduleType; }
    public void setScheduleType(String scheduleType) { this.scheduleType = scheduleType; }
    public LocalDate getExpectedDate() { return expectedDate; }
    public void setExpectedDate(LocalDate expectedDate) { this.expectedDate = expectedDate; }
    public String getVillageName() { return villageName; }
    public void setVillageName(String villageName) { this.villageName = villageName; }
    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }
    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }
    public Long getVoiceAssetId() { return voiceAssetId; }
    public void setVoiceAssetId(Long voiceAssetId) { this.voiceAssetId = voiceAssetId; }
    public String getVoiceText() { return voiceText; }
    public void setVoiceText(String voiceText) { this.voiceText = voiceText; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getRequirementJson() { return requirementJson; }
    public void setRequirementJson(String requirementJson) { this.requirementJson = requirementJson; }
    public String getGratitudeType() { return gratitudeType; }
    public void setGratitudeType(String gratitudeType) { this.gratitudeType = gratitudeType; }
    public String getPlotTags() { return plotTags; }
    public void setPlotTags(String plotTags) { this.plotTags = plotTags; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public Integer getMatchAttemptCount() { return matchAttemptCount; }
    public void setMatchAttemptCount(Integer matchAttemptCount) { this.matchAttemptCount = matchAttemptCount; }
    public Long getCurrentMatchAttemptId() { return currentMatchAttemptId; }
    public void setCurrentMatchAttemptId(Long currentMatchAttemptId) { this.currentMatchAttemptId = currentMatchAttemptId; }
    public Long getCurrentContactSessionId() { return currentContactSessionId; }
    public void setCurrentContactSessionId(Long currentContactSessionId) { this.currentContactSessionId = currentContactSessionId; }
    public Long getCurrentOrderId() { return currentOrderId; }
    public void setCurrentOrderId(Long currentOrderId) { this.currentOrderId = currentOrderId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
