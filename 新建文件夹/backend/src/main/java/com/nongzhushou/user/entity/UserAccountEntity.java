package com.nongzhushou.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("user_account")
public class UserAccountEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String phone;
    private String primaryRole;
    private String currentRole;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // === V10 新增：微信生态字段 ===
    private String wxOpenid;
    private String wxUnionid;
    private String avatarUrl;
    private String nickname;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPrimaryRole() { return primaryRole; }
    public void setPrimaryRole(String primaryRole) { this.primaryRole = primaryRole; }
    public String getCurrentRole() { return currentRole; }
    public void setCurrentRole(String currentRole) { this.currentRole = currentRole; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // === V10 新增字段 getter/setter ===
    public String getWxOpenid() { return wxOpenid; }
    public void setWxOpenid(String wxOpenid) { this.wxOpenid = wxOpenid; }
    public String getWxUnionid() { return wxUnionid; }
    public void setWxUnionid(String wxUnionid) { this.wxUnionid = wxUnionid; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
