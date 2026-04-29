package com.nongzhushou.matchflow.model;

import java.time.LocalDateTime;

public class MatchAttemptLite {
    private Long id;
    private Long taskId;
    private Long demandId;
    private Long ownerId;
    private Long serviceItemId;
    private Integer tierNo;
    private Integer distanceMeter;
    private String distanceSource;
    private Double routeDistanceKm;
    private Double straightDistanceKm;
    private Integer etaMinutes;
    private String matchReason;
    private String serviceName;
    private String villageName;
    private String distanceLayer;
    private String status;
    private LocalDateTime ownerResponseDeadline;
    private LocalDateTime farmerConfirmDeadline;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Long getServiceItemId() { return serviceItemId; }
    public void setServiceItemId(Long serviceItemId) { this.serviceItemId = serviceItemId; }
    public Integer getTierNo() { return tierNo; }
    public void setTierNo(Integer tierNo) { this.tierNo = tierNo; }
    public Integer getDistanceMeter() { return distanceMeter; }
    public void setDistanceMeter(Integer distanceMeter) { this.distanceMeter = distanceMeter; }
    public String getDistanceSource() { return distanceSource; }
    public void setDistanceSource(String distanceSource) { this.distanceSource = distanceSource; }
    public Double getRouteDistanceKm() { return routeDistanceKm; }
    public void setRouteDistanceKm(Double routeDistanceKm) { this.routeDistanceKm = routeDistanceKm; }
    public Double getStraightDistanceKm() { return straightDistanceKm; }
    public void setStraightDistanceKm(Double straightDistanceKm) { this.straightDistanceKm = straightDistanceKm; }
    public Integer getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(Integer etaMinutes) { this.etaMinutes = etaMinutes; }
    public String getMatchReason() { return matchReason; }
    public void setMatchReason(String matchReason) { this.matchReason = matchReason; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getVillageName() { return villageName; }
    public void setVillageName(String villageName) { this.villageName = villageName; }
    public String getDistanceLayer() { return distanceLayer; }
    public void setDistanceLayer(String distanceLayer) { this.distanceLayer = distanceLayer; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getOwnerResponseDeadline() { return ownerResponseDeadline; }
    public void setOwnerResponseDeadline(LocalDateTime ownerResponseDeadline) { this.ownerResponseDeadline = ownerResponseDeadline; }
    public LocalDateTime getFarmerConfirmDeadline() { return farmerConfirmDeadline; }
    public void setFarmerConfirmDeadline(LocalDateTime farmerConfirmDeadline) { this.farmerConfirmDeadline = farmerConfirmDeadline; }
}
