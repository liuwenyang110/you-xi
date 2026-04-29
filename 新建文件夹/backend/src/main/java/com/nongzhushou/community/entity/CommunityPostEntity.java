package com.nongzhushou.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("community_post")
public class CommunityPostEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long authorId;
    private String postType;       // DEMAND_URGENT / RENTAL / CHAT
    private String title;
    private String content;
    private String imagesJson;
    private String cropCode;
    private String machineCategory;
    private String machineType;
    private BigDecimal areaMu;
    private BigDecimal lat;
    private BigDecimal lng;
    private String locationName;
    private Integer isUrgent;
    private String status;
    private Integer viewCount;
    private Integer replyCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public String getPostType() { return postType; }
    public void setPostType(String postType) { this.postType = postType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImagesJson() { return imagesJson; }
    public void setImagesJson(String imagesJson) { this.imagesJson = imagesJson; }
    public String getCropCode() { return cropCode; }
    public void setCropCode(String cropCode) { this.cropCode = cropCode; }
    public String getMachineCategory() { return machineCategory; }
    public void setMachineCategory(String machineCategory) { this.machineCategory = machineCategory; }
    public String getMachineType() { return machineType; }
    public void setMachineType(String machineType) { this.machineType = machineType; }
    public BigDecimal getAreaMu() { return areaMu; }
    public void setAreaMu(BigDecimal areaMu) { this.areaMu = areaMu; }
    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }
    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public Integer getIsUrgent() { return isUrgent; }
    public void setIsUrgent(Integer isUrgent) { this.isUrgent = isUrgent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getReplyCount() { return replyCount; }
    public void setReplyCount(Integer replyCount) { this.replyCount = replyCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
