package com.nongzhushou.zone.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("v3_machine_type")
public class MachineTypeEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer categoryId;
    private String code;
    private String name;
    private String alias;
    private Integer isCrossRegion;
    private Integer isSeasonal;
    private String workSeasons;
    private String suitableCrops;
    private String terrainType;
    private String description;
    private Integer sortNo;
    private Integer enabled;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    public Integer getIsCrossRegion() { return isCrossRegion; }
    public void setIsCrossRegion(Integer isCrossRegion) { this.isCrossRegion = isCrossRegion; }
    public Integer getIsSeasonal() { return isSeasonal; }
    public void setIsSeasonal(Integer isSeasonal) { this.isSeasonal = isSeasonal; }
    public String getWorkSeasons() { return workSeasons; }
    public void setWorkSeasons(String workSeasons) { this.workSeasons = workSeasons; }
    public String getSuitableCrops() { return suitableCrops; }
    public void setSuitableCrops(String suitableCrops) { this.suitableCrops = suitableCrops; }
    public String getTerrainType() { return terrainType; }
    public void setTerrainType(String terrainType) { this.terrainType = terrainType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
}
