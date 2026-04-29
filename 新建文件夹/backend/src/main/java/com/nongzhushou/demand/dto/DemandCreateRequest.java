package com.nongzhushou.demand.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class DemandCreateRequest {

    @NotNull
    private Long serviceCategoryId;

    @NotNull
    private Long serviceSubcategoryId;

    private String cropCode;

    @NotNull(message = "areaMu is required")
    @DecimalMin(value = "0.1", message = "areaMu must be greater than 0")
    private BigDecimal areaMu;
    private String scheduleType;
    private LocalDate expectedDate;
    private Double lat;
    private Double lng;
    private String villageName;
    private Long voiceAssetId;
    private String voiceText;
    private String remark;
    private Map<String, Object> requirementJson;
    private String gratitudeType;
    private String plotTags;

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

    public String getCropCode() {
        return cropCode;
    }

    public void setCropCode(String cropCode) {
        this.cropCode = cropCode;
    }

    public BigDecimal getAreaMu() {
        return areaMu;
    }

    public void setAreaMu(BigDecimal areaMu) {
        this.areaMu = areaMu;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public LocalDate getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(LocalDate expectedDate) {
        this.expectedDate = expectedDate;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public Long getVoiceAssetId() {
        return voiceAssetId;
    }

    public void setVoiceAssetId(Long voiceAssetId) {
        this.voiceAssetId = voiceAssetId;
    }

    public String getVoiceText() {
        return voiceText;
    }

    public void setVoiceText(String voiceText) {
        this.voiceText = voiceText;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Map<String, Object> getRequirementJson() {
        return requirementJson;
    }

    public void setRequirementJson(Map<String, Object> requirementJson) {
        this.requirementJson = requirementJson;
    }

    public String getGratitudeType() {
        return gratitudeType;
    }

    public void setGratitudeType(String gratitudeType) {
        this.gratitudeType = gratitudeType;
    }

    public String getPlotTags() {
        return plotTags;
    }

    public void setPlotTags(String plotTags) {
        this.plotTags = plotTags;
    }
}
