package com.nongzhushou.v3machinery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("v3_machinery")
public class V3MachineryEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long operatorId;        // v3_user.id
    private Long zoneId;
    private Integer machineTypeId;  // v3_machine_type.id
    private String brand;           // 品牌（雷沃谷神/沃得/久保田/中联重科等）
    private String modelNo;         // 型号
    private String suitableCrops;   // 实际能收的作物，逗号分隔
    private String workTypes;       // 能提供的作业类型，逗号分隔
    private Integer isCrossRegion;  // 是否提供跨区作业
    private String crossRangeDesc;  // 跨区描述
    private String availDesc;       // 可作业时间描述
    private String photos;          // JSON数组
    private String description;
    private String status;          // ACTIVE / INACTIVE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
    public Integer getMachineTypeId() { return machineTypeId; }
    public void setMachineTypeId(Integer machineTypeId) { this.machineTypeId = machineTypeId; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModelNo() { return modelNo; }
    public void setModelNo(String modelNo) { this.modelNo = modelNo; }
    public String getSuitableCrops() { return suitableCrops; }
    public void setSuitableCrops(String suitableCrops) { this.suitableCrops = suitableCrops; }
    public String getWorkTypes() { return workTypes; }
    public void setWorkTypes(String workTypes) { this.workTypes = workTypes; }
    public Integer getIsCrossRegion() { return isCrossRegion; }
    public void setIsCrossRegion(Integer isCrossRegion) { this.isCrossRegion = isCrossRegion; }
    public String getCrossRangeDesc() { return crossRangeDesc; }
    public void setCrossRangeDesc(String crossRangeDesc) { this.crossRangeDesc = crossRangeDesc; }
    public String getAvailDesc() { return availDesc; }
    public void setAvailDesc(String availDesc) { this.availDesc = availDesc; }
    public String getPhotos() { return photos; }
    public void setPhotos(String photos) { this.photos = photos; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
