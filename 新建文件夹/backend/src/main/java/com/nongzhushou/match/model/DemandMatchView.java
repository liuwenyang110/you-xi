package com.nongzhushou.match.model;

import java.math.BigDecimal;
import java.util.Set;

public class DemandMatchView {
    private Long demandId;
    private Long serviceCategoryId;
    private Long serviceSubcategoryId;
    private String cropCode;
    private BigDecimal areaMu;
    private Double lat;
    private Double lng;
    private String requirementJson;
    private Set<String> terrainTags;
    private Set<String> plotTags;

    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
    public Long getServiceCategoryId() { return serviceCategoryId; }
    public void setServiceCategoryId(Long serviceCategoryId) { this.serviceCategoryId = serviceCategoryId; }
    public Long getServiceSubcategoryId() { return serviceSubcategoryId; }
    public void setServiceSubcategoryId(Long serviceSubcategoryId) { this.serviceSubcategoryId = serviceSubcategoryId; }
    public String getCropCode() { return cropCode; }
    public void setCropCode(String cropCode) { this.cropCode = cropCode; }
    public BigDecimal getAreaMu() { return areaMu; }
    public void setAreaMu(BigDecimal areaMu) { this.areaMu = areaMu; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public String getRequirementJson() { return requirementJson; }
    public void setRequirementJson(String requirementJson) { this.requirementJson = requirementJson; }
    public Set<String> getTerrainTags() { return terrainTags; }
    public void setTerrainTags(Set<String> terrainTags) { this.terrainTags = terrainTags; }
    public Set<String> getPlotTags() { return plotTags; }
    public void setPlotTags(Set<String> plotTags) { this.plotTags = plotTags; }
}
