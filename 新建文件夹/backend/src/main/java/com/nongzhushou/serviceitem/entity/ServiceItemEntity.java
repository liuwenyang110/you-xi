package com.nongzhushou.serviceitem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("service_item")
public class ServiceItemEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ownerId;
    private Long serviceCategoryId;
    private Long serviceSubcategoryId;
    private String serviceName;
    private String machineBindingType;
    private Long mainEquipmentId;
    private String relatedEquipmentIds;
    private String cropTags;
    private String terrainTags;
    private String plotTags;
    private BigDecimal minAreaMu;
    private BigDecimal maxAreaMu;
    private String availableTimeDesc;
    private Boolean isAcceptingOrders;
    private Integer serviceRadiusKm;
    private String approveStatus;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Long getServiceCategoryId() { return serviceCategoryId; }
    public void setServiceCategoryId(Long serviceCategoryId) { this.serviceCategoryId = serviceCategoryId; }
    public Long getServiceSubcategoryId() { return serviceSubcategoryId; }
    public void setServiceSubcategoryId(Long serviceSubcategoryId) { this.serviceSubcategoryId = serviceSubcategoryId; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getMachineBindingType() { return machineBindingType; }
    public void setMachineBindingType(String machineBindingType) { this.machineBindingType = machineBindingType; }
    public Long getMainEquipmentId() { return mainEquipmentId; }
    public void setMainEquipmentId(Long mainEquipmentId) { this.mainEquipmentId = mainEquipmentId; }
    public String getRelatedEquipmentIds() { return relatedEquipmentIds; }
    public void setRelatedEquipmentIds(String relatedEquipmentIds) { this.relatedEquipmentIds = relatedEquipmentIds; }
    public String getCropTags() { return cropTags; }
    public void setCropTags(String cropTags) { this.cropTags = cropTags; }
    public String getTerrainTags() { return terrainTags; }
    public void setTerrainTags(String terrainTags) { this.terrainTags = terrainTags; }
    public String getPlotTags() { return plotTags; }
    public void setPlotTags(String plotTags) { this.plotTags = plotTags; }
    public BigDecimal getMinAreaMu() { return minAreaMu; }
    public void setMinAreaMu(BigDecimal minAreaMu) { this.minAreaMu = minAreaMu; }
    public BigDecimal getMaxAreaMu() { return maxAreaMu; }
    public void setMaxAreaMu(BigDecimal maxAreaMu) { this.maxAreaMu = maxAreaMu; }
    public String getAvailableTimeDesc() { return availableTimeDesc; }
    public void setAvailableTimeDesc(String availableTimeDesc) { this.availableTimeDesc = availableTimeDesc; }
    public Boolean getIsAcceptingOrders() { return isAcceptingOrders; }
    public void setIsAcceptingOrders(Boolean isAcceptingOrders) { this.isAcceptingOrders = isAcceptingOrders; }
    public Integer getServiceRadiusKm() { return serviceRadiusKm; }
    public void setServiceRadiusKm(Integer serviceRadiusKm) { this.serviceRadiusKm = serviceRadiusKm; }
    public String getApproveStatus() { return approveStatus; }
    public void setApproveStatus(String approveStatus) { this.approveStatus = approveStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
