package com.nongzhushou.v3demand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("v3_demand")
public class V3DemandEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long farmerId;
    private Long zoneId;
    private Integer workTypeId;
    private Integer cropId;
    private String areaDesc;
    private LocalDate expectDateStart;
    private LocalDate expectDateEnd;
    private String locationDesc;
    private String plotNotes;
    private String photos;
    private Long groupId;
    private String status;           // PUBLISHED / CONTACTED / COMPLETED / CANCELLED
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // 生命周期追踪字段
    private LocalDateTime firstAskAt;
    private String firstAskReply;
    private LocalDateTime secondAskAt;
    private String secondAskReply;
    private LocalDateTime finalWarnAt;
    private Integer autoCleaned;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }
    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
    public Integer getWorkTypeId() { return workTypeId; }
    public void setWorkTypeId(Integer workTypeId) { this.workTypeId = workTypeId; }
    public Integer getCropId() { return cropId; }
    public void setCropId(Integer cropId) { this.cropId = cropId; }
    public String getAreaDesc() { return areaDesc; }
    public void setAreaDesc(String areaDesc) { this.areaDesc = areaDesc; }
    public LocalDate getExpectDateStart() { return expectDateStart; }
    public void setExpectDateStart(LocalDate expectDateStart) { this.expectDateStart = expectDateStart; }
    public LocalDate getExpectDateEnd() { return expectDateEnd; }
    public void setExpectDateEnd(LocalDate expectDateEnd) { this.expectDateEnd = expectDateEnd; }
    public String getLocationDesc() { return locationDesc; }
    public void setLocationDesc(String locationDesc) { this.locationDesc = locationDesc; }
    public String getPlotNotes() { return plotNotes; }
    public void setPlotNotes(String plotNotes) { this.plotNotes = plotNotes; }
    public String getPhotos() { return photos; }
    public void setPhotos(String photos) { this.photos = photos; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    // 生命周期 getter/setter
    public LocalDateTime getFirstAskAt() { return firstAskAt; }
    public void setFirstAskAt(LocalDateTime firstAskAt) { this.firstAskAt = firstAskAt; }
    public String getFirstAskReply() { return firstAskReply; }
    public void setFirstAskReply(String firstAskReply) { this.firstAskReply = firstAskReply; }
    public LocalDateTime getSecondAskAt() { return secondAskAt; }
    public void setSecondAskAt(LocalDateTime secondAskAt) { this.secondAskAt = secondAskAt; }
    public String getSecondAskReply() { return secondAskReply; }
    public void setSecondAskReply(String secondAskReply) { this.secondAskReply = secondAskReply; }
    public LocalDateTime getFinalWarnAt() { return finalWarnAt; }
    public void setFinalWarnAt(LocalDateTime finalWarnAt) { this.finalWarnAt = finalWarnAt; }
    public Integer getAutoCleaned() { return autoCleaned; }
    public void setAutoCleaned(Integer autoCleaned) { this.autoCleaned = autoCleaned; }
}
