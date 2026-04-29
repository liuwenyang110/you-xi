package com.nongzhushou.v3machinery.dto;

import jakarta.validation.constraints.NotNull;

public class V3MachineryRequest {

    @NotNull(message = "机型不能为空")
    private Integer machineTypeId;

    private String brand;           // 品牌，如"雷沃谷神"
    private String modelNo;         // 型号，如"GE80"
    private String suitableCrops;   // 适用作物编码，逗号分隔
    private String workTypes;       // 能提供的作业类型，逗号分隔
    private Integer isCrossRegion;  // 0=本地 1=跨区
    private String crossRangeDesc;  // 跨区描述，如"可跨省，5月河南6月山东"
    private String availDesc;       // 可作业时间描述
    private String photos;          // JSON数组字符串
    private String description;     // 附加说明

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
}
