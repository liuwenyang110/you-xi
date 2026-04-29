package com.nongzhushou.v3user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("v3_user")
public class V3UserEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long accountId;          // 关联 user_account.id
    private String role;             // FARMER / OPERATOR
    private String realName;
    private Integer verified;        // 0=未认证 1=已认证
    private Long zoneId;             // 所属片区
    private LocalDateTime zoneJoinedAt;
    private LocalDateTime zoneChangedAt;
    private String homeLocation;
    private String virtualPhone;
    private Long agentAccountId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Integer getVerified() { return verified; }
    public void setVerified(Integer verified) { this.verified = verified; }
    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
    public LocalDateTime getZoneJoinedAt() { return zoneJoinedAt; }
    public void setZoneJoinedAt(LocalDateTime zoneJoinedAt) { this.zoneJoinedAt = zoneJoinedAt; }
    public LocalDateTime getZoneChangedAt() { return zoneChangedAt; }
    public void setZoneChangedAt(LocalDateTime zoneChangedAt) { this.zoneChangedAt = zoneChangedAt; }
    public String getHomeLocation() { return homeLocation; }
    public void setHomeLocation(String homeLocation) { this.homeLocation = homeLocation; }
    public String getVirtualPhone() { return virtualPhone; }
    public void setVirtualPhone(String virtualPhone) { this.virtualPhone = virtualPhone; }
    public Long getAgentAccountId() { return agentAccountId; }
    public void setAgentAccountId(Long agentAccountId) { this.agentAccountId = agentAccountId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
