package com.nongzhushou.match.model;

import java.math.BigDecimal;
import java.util.Set;

public class MatchRuleInput {
    private Long demandId;
    private Long ownerId;
    private Long serviceItemId;
    private String cropCode;
    private BigDecimal areaMu;
    private Double demandLat;
    private Double demandLng;
    private Integer tierNo;
    private Integer maxDistanceKm;
    private Set<String> terrainTags;
    private Set<String> plotTags;
    private Set<String> orderConflictStatuses;
    private Boolean ownerAccepting;
    private Boolean serviceItemActive;
    private Boolean realnamePassed;
    private Boolean equipmentIdle;

    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Long getServiceItemId() { return serviceItemId; }
    public void setServiceItemId(Long serviceItemId) { this.serviceItemId = serviceItemId; }
    public String getCropCode() { return cropCode; }
    public void setCropCode(String cropCode) { this.cropCode = cropCode; }
    public BigDecimal getAreaMu() { return areaMu; }
    public void setAreaMu(BigDecimal areaMu) { this.areaMu = areaMu; }
    public Double getDemandLat() { return demandLat; }
    public void setDemandLat(Double demandLat) { this.demandLat = demandLat; }
    public Double getDemandLng() { return demandLng; }
    public void setDemandLng(Double demandLng) { this.demandLng = demandLng; }
    public Integer getTierNo() { return tierNo; }
    public void setTierNo(Integer tierNo) { this.tierNo = tierNo; }
    public Integer getMaxDistanceKm() { return maxDistanceKm; }
    public void setMaxDistanceKm(Integer maxDistanceKm) { this.maxDistanceKm = maxDistanceKm; }
    public Set<String> getTerrainTags() { return terrainTags; }
    public void setTerrainTags(Set<String> terrainTags) { this.terrainTags = terrainTags; }
    public Set<String> getPlotTags() { return plotTags; }
    public void setPlotTags(Set<String> plotTags) { this.plotTags = plotTags; }
    public Set<String> getOrderConflictStatuses() { return orderConflictStatuses; }
    public void setOrderConflictStatuses(Set<String> orderConflictStatuses) { this.orderConflictStatuses = orderConflictStatuses; }
    public Boolean getOwnerAccepting() { return ownerAccepting; }
    public void setOwnerAccepting(Boolean ownerAccepting) { this.ownerAccepting = ownerAccepting; }
    public Boolean getServiceItemActive() { return serviceItemActive; }
    public void setServiceItemActive(Boolean serviceItemActive) { this.serviceItemActive = serviceItemActive; }
    public Boolean getRealnamePassed() { return realnamePassed; }
    public void setRealnamePassed(Boolean realnamePassed) { this.realnamePassed = realnamePassed; }
    public Boolean getEquipmentIdle() { return equipmentIdle; }
    public void setEquipmentIdle(Boolean equipmentIdle) { this.equipmentIdle = equipmentIdle; }
}
