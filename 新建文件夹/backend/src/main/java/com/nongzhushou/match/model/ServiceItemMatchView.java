package com.nongzhushou.match.model;

import java.math.BigDecimal;
import java.util.Set;

public class ServiceItemMatchView {
    private Long serviceItemId;
    private Long ownerId;
    private Long serviceCategoryId;
    private Long serviceSubcategoryId;
    private Long mainEquipmentId;
    private Boolean ownerAccepting;
    private Boolean serviceItemActive;
    private Boolean realnamePassed;
    private Boolean equipmentIdle;
    private Double ownerLat;
    private Double ownerLng;
    private Integer serviceRadiusKm;
    private BigDecimal minAreaMu;
    private BigDecimal maxAreaMu;
    private String cropTagsRaw;
    private String terrainTagsRaw;
    private String plotTagsRaw;
    private Set<String> cropTags;
    private Set<String> terrainTags;
    private Set<String> plotTags;

    public Long getServiceItemId() { return serviceItemId; }
    public void setServiceItemId(Long serviceItemId) { this.serviceItemId = serviceItemId; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Long getServiceCategoryId() { return serviceCategoryId; }
    public void setServiceCategoryId(Long serviceCategoryId) { this.serviceCategoryId = serviceCategoryId; }
    public Long getServiceSubcategoryId() { return serviceSubcategoryId; }
    public void setServiceSubcategoryId(Long serviceSubcategoryId) { this.serviceSubcategoryId = serviceSubcategoryId; }
    public Long getMainEquipmentId() { return mainEquipmentId; }
    public void setMainEquipmentId(Long mainEquipmentId) { this.mainEquipmentId = mainEquipmentId; }
    public Boolean getOwnerAccepting() { return ownerAccepting; }
    public void setOwnerAccepting(Boolean ownerAccepting) { this.ownerAccepting = ownerAccepting; }
    public Boolean getServiceItemActive() { return serviceItemActive; }
    public void setServiceItemActive(Boolean serviceItemActive) { this.serviceItemActive = serviceItemActive; }
    public Boolean getRealnamePassed() { return realnamePassed; }
    public void setRealnamePassed(Boolean realnamePassed) { this.realnamePassed = realnamePassed; }
    public Boolean getEquipmentIdle() { return equipmentIdle; }
    public void setEquipmentIdle(Boolean equipmentIdle) { this.equipmentIdle = equipmentIdle; }
    public Double getOwnerLat() { return ownerLat; }
    public void setOwnerLat(Double ownerLat) { this.ownerLat = ownerLat; }
    public Double getOwnerLng() { return ownerLng; }
    public void setOwnerLng(Double ownerLng) { this.ownerLng = ownerLng; }
    public Integer getServiceRadiusKm() { return serviceRadiusKm; }
    public void setServiceRadiusKm(Integer serviceRadiusKm) { this.serviceRadiusKm = serviceRadiusKm; }
    public BigDecimal getMinAreaMu() { return minAreaMu; }
    public void setMinAreaMu(BigDecimal minAreaMu) { this.minAreaMu = minAreaMu; }
    public BigDecimal getMaxAreaMu() { return maxAreaMu; }
    public void setMaxAreaMu(BigDecimal maxAreaMu) { this.maxAreaMu = maxAreaMu; }
    public String getCropTagsRaw() { return cropTagsRaw; }
    public void setCropTagsRaw(String cropTagsRaw) { this.cropTagsRaw = cropTagsRaw; }
    public String getTerrainTagsRaw() { return terrainTagsRaw; }
    public void setTerrainTagsRaw(String terrainTagsRaw) { this.terrainTagsRaw = terrainTagsRaw; }
    public String getPlotTagsRaw() { return plotTagsRaw; }
    public void setPlotTagsRaw(String plotTagsRaw) { this.plotTagsRaw = plotTagsRaw; }
    public Set<String> getCropTags() { return cropTags; }
    public void setCropTags(Set<String> cropTags) { this.cropTags = cropTags; }
    public Set<String> getTerrainTags() { return terrainTags; }
    public void setTerrainTags(Set<String> terrainTags) { this.terrainTags = terrainTags; }
    public Set<String> getPlotTags() { return plotTags; }
    public void setPlotTags(Set<String> plotTags) { this.plotTags = plotTags; }
}
