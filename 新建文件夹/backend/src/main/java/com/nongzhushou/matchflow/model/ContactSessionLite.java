package com.nongzhushou.matchflow.model;

import java.time.LocalDateTime;

public class ContactSessionLite {
    private Long id;
    private Long demandId;
    private Long ownerId;
    private Long farmerId;
    private Long serviceItemId;
    private Long matchAttemptId;
    private String sessionStatus;
    private String contactMode;
    private LocalDateTime expireAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }
    public Long getServiceItemId() { return serviceItemId; }
    public void setServiceItemId(Long serviceItemId) { this.serviceItemId = serviceItemId; }
    public Long getMatchAttemptId() { return matchAttemptId; }
    public void setMatchAttemptId(Long matchAttemptId) { this.matchAttemptId = matchAttemptId; }
    public String getSessionStatus() { return sessionStatus; }
    public void setSessionStatus(String sessionStatus) { this.sessionStatus = sessionStatus; }
    public String getContactMode() { return contactMode; }
    public void setContactMode(String contactMode) { this.contactMode = contactMode; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
}
