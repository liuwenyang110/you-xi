package com.nongzhushou.serviceitem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ServiceItemCreateRequest {

    @NotNull
    @Positive(message = "serviceCategoryId must be greater than 0")
    private Long serviceCategoryId;

    @NotNull
    @Positive(message = "serviceSubcategoryId must be greater than 0")
    private Long serviceSubcategoryId;

    @NotBlank
    private String serviceName;

    @NotBlank
    private String machineBindingType;

    @NotNull
    @Positive(message = "mainEquipmentId must be greater than 0")
    private Long mainEquipmentId;

    @Size(max = 20, message = "relatedEquipmentIds cannot exceed 20 items")
    private List<Long> relatedEquipmentIds = new ArrayList<>();
    private List<String> cropTags = new ArrayList<>();
    private List<String> terrainTags = new ArrayList<>();
    private List<String> plotTags = new ArrayList<>();
    @DecimalMin(value = "0.0", inclusive = false, message = "minAreaMu must be greater than 0")
    private BigDecimal minAreaMu;
    @DecimalMin(value = "0.0", inclusive = false, message = "maxAreaMu must be greater than 0")
    private BigDecimal maxAreaMu;
    private String availableTimeDesc;
    @Min(value = 1, message = "serviceRadiusKm must be at least 1")
    private Integer serviceRadiusKm = 15;
    private Boolean isAcceptingOrders = true;

    public Long getServiceCategoryId() {
        return serviceCategoryId;
    }

    public void setServiceCategoryId(Long serviceCategoryId) {
        this.serviceCategoryId = serviceCategoryId;
    }

    public Long getServiceSubcategoryId() {
        return serviceSubcategoryId;
    }

    public void setServiceSubcategoryId(Long serviceSubcategoryId) {
        this.serviceSubcategoryId = serviceSubcategoryId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMachineBindingType() {
        return machineBindingType;
    }

    public void setMachineBindingType(String machineBindingType) {
        this.machineBindingType = machineBindingType;
    }

    public Long getMainEquipmentId() {
        return mainEquipmentId;
    }

    public void setMainEquipmentId(Long mainEquipmentId) {
        this.mainEquipmentId = mainEquipmentId;
    }

    public List<Long> getRelatedEquipmentIds() {
        return relatedEquipmentIds;
    }

    public void setRelatedEquipmentIds(List<Long> relatedEquipmentIds) {
        this.relatedEquipmentIds = relatedEquipmentIds == null ? new ArrayList<>() : relatedEquipmentIds;
    }

    public List<String> getCropTags() {
        return cropTags;
    }

    public void setCropTags(List<String> cropTags) {
        this.cropTags = cropTags == null ? new ArrayList<>() : cropTags;
    }

    public List<String> getTerrainTags() {
        return terrainTags;
    }

    public void setTerrainTags(List<String> terrainTags) {
        this.terrainTags = terrainTags == null ? new ArrayList<>() : terrainTags;
    }

    public List<String> getPlotTags() {
        return plotTags;
    }

    public void setPlotTags(List<String> plotTags) {
        this.plotTags = plotTags == null ? new ArrayList<>() : plotTags;
    }

    public BigDecimal getMinAreaMu() {
        return minAreaMu;
    }

    public void setMinAreaMu(BigDecimal minAreaMu) {
        this.minAreaMu = minAreaMu;
    }

    public BigDecimal getMaxAreaMu() {
        return maxAreaMu;
    }

    public void setMaxAreaMu(BigDecimal maxAreaMu) {
        this.maxAreaMu = maxAreaMu;
    }

    public String getAvailableTimeDesc() {
        return availableTimeDesc;
    }

    public void setAvailableTimeDesc(String availableTimeDesc) {
        this.availableTimeDesc = availableTimeDesc;
    }

    public Integer getServiceRadiusKm() {
        return serviceRadiusKm;
    }

    public void setServiceRadiusKm(Integer serviceRadiusKm) {
        this.serviceRadiusKm = serviceRadiusKm;
    }

    public Boolean getIsAcceptingOrders() {
        return isAcceptingOrders;
    }

    public void setIsAcceptingOrders(Boolean isAcceptingOrders) {
        this.isAcceptingOrders = isAcceptingOrders;
    }
}
