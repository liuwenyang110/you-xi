package com.nongzhushou.zone.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("v3_crop")
public class CropEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String code;
    private String name;
    private String alias;
    private String cropGroup;
    private String mainRegions;
    private String harvestSeasons;
    private Integer sortNo;
    private Integer enabled;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    public String getCropGroup() { return cropGroup; }
    public void setCropGroup(String cropGroup) { this.cropGroup = cropGroup; }
    public String getMainRegions() { return mainRegions; }
    public void setMainRegions(String mainRegions) { this.mainRegions = mainRegions; }
    public String getHarvestSeasons() { return harvestSeasons; }
    public void setHarvestSeasons(String harvestSeasons) { this.harvestSeasons = harvestSeasons; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
}
