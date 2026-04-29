package com.nongzhushou.zone.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("v3_machine_category")
public class MachineCategoryEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String code;
    private String name;
    private String icon;
    private String rangeType; // CROSS_PROVINCE / CROSS_COUNTY / LOCAL
    private Integer sortNo;
    private Integer enabled;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getRangeType() { return rangeType; }
    public void setRangeType(String rangeType) { this.rangeType = rangeType; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
}
