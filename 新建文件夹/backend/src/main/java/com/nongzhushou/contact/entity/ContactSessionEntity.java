package com.nongzhushou.contact.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("contact_session")
public class ContactSessionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long demandId;
    private Long matchAttemptId;
    private Long ownerId;
    private Long farmerId;
    private Long serviceItemId;
    private String status;
    private Integer activeFlag;
    private String maskedPhone;
    private String contactMode;
    private LocalDateTime expireAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String contactType;
    private String farmerFeedback;
    private Integer farmerRating;
    private Integer serviceCompleted;
    private String discoverySource;
    private String sessionType;
    private Long sourcePostId;
    private Long volunteerClaimId;
    private String subject;
    private String summary;
    private String deliveryMode;
    private String lastMessagePreview;
    private Long lastSenderId;
    private Integer unreadFarmerCount;
    private Integer unreadOwnerCount;
    private LocalDateTime lastMessageAt;
    private LocalDateTime lastAckAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
    public Long getMatchAttemptId() { return matchAttemptId; }
    public void setMatchAttemptId(Long matchAttemptId) { this.matchAttemptId = matchAttemptId; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }
    public Long getServiceItemId() { return serviceItemId; }
    public void setServiceItemId(Long serviceItemId) { this.serviceItemId = serviceItemId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getActiveFlag() { return activeFlag; }
    public void setActiveFlag(Integer activeFlag) { this.activeFlag = activeFlag; }
    public String getMaskedPhone() { return maskedPhone; }
    public void setMaskedPhone(String maskedPhone) { this.maskedPhone = maskedPhone; }
    public String getContactMode() { return contactMode; }
    public void setContactMode(String contactMode) { this.contactMode = contactMode; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getContactType() { return contactType; }
    public void setContactType(String contactType) { this.contactType = contactType; }
    public String getFarmerFeedback() { return farmerFeedback; }
    public void setFarmerFeedback(String farmerFeedback) { this.farmerFeedback = farmerFeedback; }
    public Integer getFarmerRating() { return farmerRating; }
    public void setFarmerRating(Integer farmerRating) { this.farmerRating = farmerRating; }
    public Integer getServiceCompleted() { return serviceCompleted; }
    public void setServiceCompleted(Integer serviceCompleted) { this.serviceCompleted = serviceCompleted; }
    public String getDiscoverySource() { return discoverySource; }
    public void setDiscoverySource(String discoverySource) { this.discoverySource = discoverySource; }
    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }
    public Long getSourcePostId() { return sourcePostId; }
    public void setSourcePostId(Long sourcePostId) { this.sourcePostId = sourcePostId; }
    public Long getVolunteerClaimId() { return volunteerClaimId; }
    public void setVolunteerClaimId(Long volunteerClaimId) { this.volunteerClaimId = volunteerClaimId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getDeliveryMode() { return deliveryMode; }
    public void setDeliveryMode(String deliveryMode) { this.deliveryMode = deliveryMode; }
    public String getLastMessagePreview() { return lastMessagePreview; }
    public void setLastMessagePreview(String lastMessagePreview) { this.lastMessagePreview = lastMessagePreview; }
    public Long getLastSenderId() { return lastSenderId; }
    public void setLastSenderId(Long lastSenderId) { this.lastSenderId = lastSenderId; }
    public Integer getUnreadFarmerCount() { return unreadFarmerCount; }
    public void setUnreadFarmerCount(Integer unreadFarmerCount) { this.unreadFarmerCount = unreadFarmerCount; }
    public Integer getUnreadOwnerCount() { return unreadOwnerCount; }
    public void setUnreadOwnerCount(Integer unreadOwnerCount) { this.unreadOwnerCount = unreadOwnerCount; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public LocalDateTime getLastAckAt() { return lastAckAt; }
    public void setLastAckAt(LocalDateTime lastAckAt) { this.lastAckAt = lastAckAt; }
}
