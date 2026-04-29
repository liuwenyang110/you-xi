package com.nongzhushou.zone.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("v3_work_type")
public class WorkTypeEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String code;
    private String name;
    private Integer categoryId;
    private String suitableCrops;
    private String description;
    private Integer sortNo;
    private Integer enabled;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getSuitableCrops() { return suitableCrops; }
    public void setSuitableCrops(String suitableCrops) { this.suitableCrops = suitableCrops; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
}
