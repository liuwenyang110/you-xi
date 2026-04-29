package com.nongzhushou.v3demand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("v3_demand_group")
public class V3DemandGroupEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long zoneId;
    private Long creatorId;
    private Integer workTypeId;
    private Integer cropId;
    private String title;
    private String totalAreaDesc;
    private LocalDate expectDateStart;
    private LocalDate expectDateEnd;
    private String locationDesc;
    private Integer memberCount;
    private String status;           // OPEN / CLOSED / COMPLETED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public Integer getWorkTypeId() { return workTypeId; }
    public void setWorkTypeId(Integer workTypeId) { this.workTypeId = workTypeId; }
    public Integer getCropId() { return cropId; }
    public void setCropId(Integer cropId) { this.cropId = cropId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTotalAreaDesc() { return totalAreaDesc; }
    public void setTotalAreaDesc(String totalAreaDesc) { this.totalAreaDesc = totalAreaDesc; }
    public LocalDate getExpectDateStart() { return expectDateStart; }
    public void setExpectDateStart(LocalDate expectDateStart) { this.expectDateStart = expectDateStart; }
    public LocalDate getExpectDateEnd() { return expectDateEnd; }
    public void setExpectDateEnd(LocalDate expectDateEnd) { this.expectDateEnd = expectDateEnd; }
    public String getLocationDesc() { return locationDesc; }
    public void setLocationDesc(String locationDesc) { this.locationDesc = locationDesc; }
    public Integer getMemberCount() { return memberCount; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
