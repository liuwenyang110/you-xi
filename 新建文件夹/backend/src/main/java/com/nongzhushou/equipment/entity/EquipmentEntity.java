package com.nongzhushou.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("equipment")
public class EquipmentEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ownerId;
    private Long machineTypeId;
    private String equipmentName;
    private String brandModel;
    private Integer quantity;
    private String currentStatus;
    private String baseRegionCode;
    private Integer serviceRadiusKm;
    private BigDecimal currentLat;
    private BigDecimal currentLng;
    private String approveStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Long getMachineTypeId() { return machineTypeId; }
    public void setMachineTypeId(Long machineTypeId) { this.machineTypeId = machineTypeId; }
    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
    public String getBrandModel() { return brandModel; }
    public void setBrandModel(String brandModel) { this.brandModel = brandModel; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
    public String getBaseRegionCode() { return baseRegionCode; }
    public void setBaseRegionCode(String baseRegionCode) { this.baseRegionCode = baseRegionCode; }
    public Integer getServiceRadiusKm() { return serviceRadiusKm; }
    public void setServiceRadiusKm(Integer serviceRadiusKm) { this.serviceRadiusKm = serviceRadiusKm; }
    public BigDecimal getCurrentLat() { return currentLat; }
    public void setCurrentLat(BigDecimal currentLat) { this.currentLat = currentLat; }
    public BigDecimal getCurrentLng() { return currentLng; }
    public void setCurrentLng(BigDecimal currentLng) { this.currentLng = currentLng; }
    public String getApproveStatus() { return approveStatus; }
    public void setApproveStatus(String approveStatus) { this.approveStatus = approveStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
