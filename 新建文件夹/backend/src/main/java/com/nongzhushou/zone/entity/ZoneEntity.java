package com.nongzhushou.zone.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("v3_zone")
public class ZoneEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String zoneType;       // VILLAGE / COMMUNITY / FARM
    private String townshipCode;
    private String countyCode;
    private String cityCode;
    private String provinceCode;
    private String description;
    private String locationDesc;    // 位置描述（自然语言）
    private Integer operatorCount;
    private Integer machineryCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getZoneType() { return zoneType; }
    public void setZoneType(String zoneType) { this.zoneType = zoneType; }
    public String getTownshipCode() { return townshipCode; }
    public void setTownshipCode(String townshipCode) { this.townshipCode = townshipCode; }
    public String getCountyCode() { return countyCode; }
    public void setCountyCode(String countyCode) { this.countyCode = countyCode; }
    public String getCityCode() { return cityCode; }
    public void setCityCode(String cityCode) { this.cityCode = cityCode; }
    public String getProvinceCode() { return provinceCode; }
    public void setProvinceCode(String provinceCode) { this.provinceCode = provinceCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocationDesc() { return locationDesc; }
    public void setLocationDesc(String locationDesc) { this.locationDesc = locationDesc; }
    public Integer getOperatorCount() { return operatorCount; }
    public void setOperatorCount(Integer operatorCount) { this.operatorCount = operatorCount; }
    public Integer getMachineryCount() { return machineryCount; }
    public void setMachineryCount(Integer machineryCount) { this.machineryCount = machineryCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
