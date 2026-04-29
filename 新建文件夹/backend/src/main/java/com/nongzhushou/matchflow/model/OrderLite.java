package com.nongzhushou.matchflow.model;

public class OrderLite {
    private Long id;
    private Long demandId;
    private Long matchAttemptId;
    private Long farmerId;
    private Long ownerId;
    private Long serviceItemId;
    private Long contactSessionId;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
    public Long getMatchAttemptId() { return matchAttemptId; }
    public void setMatchAttemptId(Long matchAttemptId) { this.matchAttemptId = matchAttemptId; }
    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Long getServiceItemId() { return serviceItemId; }
    public void setServiceItemId(Long serviceItemId) { this.serviceItemId = serviceItemId; }
    public Long getContactSessionId() { return contactSessionId; }
    public void setContactSessionId(Long contactSessionId) { this.contactSessionId = contactSessionId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
