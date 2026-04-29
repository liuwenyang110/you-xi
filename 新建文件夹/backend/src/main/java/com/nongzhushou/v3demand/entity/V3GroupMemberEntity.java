package com.nongzhushou.v3demand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("v3_group_member")
public class V3GroupMemberEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long groupId;
    private Long farmerId;
    private String areaDesc;
    private String locationDesc;
    private String joinStatus;   // PENDING / APPROVED / REJECTED
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }
    public String getAreaDesc() { return areaDesc; }
    public void setAreaDesc(String areaDesc) { this.areaDesc = areaDesc; }
    public String getLocationDesc() { return locationDesc; }
    public void setLocationDesc(String locationDesc) { this.locationDesc = locationDesc; }
    public String getJoinStatus() { return joinStatus; }
    public void setJoinStatus(String joinStatus) { this.joinStatus = joinStatus; }
    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
