package com.nongzhushou.v3demand.dto;

public class DemandGroupCreateDTO {
    private Integer workTypeId;
    private Integer cropId;
    private String title;
    private String totalAreaDesc;
    private String locationDesc;
    private String expectDateStart;
    private String expectDateEnd;
    private String myAreaDesc;
    private String myLocationDesc;

    // Getters and Setters
    public Integer getWorkTypeId() { return workTypeId; }
    public void setWorkTypeId(Integer workTypeId) { this.workTypeId = workTypeId; }
    public Integer getCropId() { return cropId; }
    public void setCropId(Integer cropId) { this.cropId = cropId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTotalAreaDesc() { return totalAreaDesc; }
    public void setTotalAreaDesc(String totalAreaDesc) { this.totalAreaDesc = totalAreaDesc; }
    public String getLocationDesc() { return locationDesc; }
    public void setLocationDesc(String locationDesc) { this.locationDesc = locationDesc; }
    public String getExpectDateStart() { return expectDateStart; }
    public void setExpectDateStart(String expectDateStart) { this.expectDateStart = expectDateStart; }
    public String getExpectDateEnd() { return expectDateEnd; }
    public void setExpectDateEnd(String expectDateEnd) { this.expectDateEnd = expectDateEnd; }
    public String getMyAreaDesc() { return myAreaDesc; }
    public void setMyAreaDesc(String myAreaDesc) { this.myAreaDesc = myAreaDesc; }
    public String getMyLocationDesc() { return myLocationDesc; }
    public void setMyLocationDesc(String myLocationDesc) { this.myLocationDesc = myLocationDesc; }
}
