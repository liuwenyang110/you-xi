package com.nongzhushou.equipment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public class EquipmentCreateRequest {

    @NotNull
    @Positive(message = "machineTypeId must be greater than 0")
    private Long machineTypeId;

    @NotBlank
    private String equipmentName;

    private String brandModel;

    @Min(1)
    private Integer quantity = 1;

    private String baseRegionCode;

    @Min(value = 1, message = "serviceRadiusKm must be at least 1")
    private Integer serviceRadiusKm = 15;

    @DecimalMin(value = "-90.0", message = "currentLat must be >= -90")
    @DecimalMax(value = "90.0", message = "currentLat must be <= 90")
    private Double currentLat;

    @DecimalMin(value = "-180.0", message = "currentLng must be >= -180")
    @DecimalMax(value = "180.0", message = "currentLng must be <= 180")
    private Double currentLng;

    public Long getMachineTypeId() {
        return machineTypeId;
    }

    public void setMachineTypeId(Long machineTypeId) {
        this.machineTypeId = machineTypeId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getBrandModel() {
        return brandModel;
    }

    public void setBrandModel(String brandModel) {
        this.brandModel = brandModel;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getBaseRegionCode() {
        return baseRegionCode;
    }

    public void setBaseRegionCode(String baseRegionCode) {
        this.baseRegionCode = baseRegionCode;
    }

    public Integer getServiceRadiusKm() {
        return serviceRadiusKm;
    }

    public void setServiceRadiusKm(Integer serviceRadiusKm) {
        this.serviceRadiusKm = serviceRadiusKm;
    }

    public Double getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(Double currentLat) {
        this.currentLat = currentLat;
    }

    public Double getCurrentLng() {
        return currentLng;
    }

    public void setCurrentLng(Double currentLng) {
        this.currentLng = currentLng;
    }
}
