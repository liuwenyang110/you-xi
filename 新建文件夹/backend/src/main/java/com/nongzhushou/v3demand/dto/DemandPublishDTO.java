package com.nongzhushou.v3demand.dto;

import java.util.Map;

public class DemandPublishDTO {
    private Integer workTypeId;
    private Integer cropId;
    private String areaDesc;
    private String locationDesc;
    private String plotNotes;
    private String photos;
    private String expectDateStart;
    private String expectDateEnd;

    // Getters and Setters
    public Integer getWorkTypeId() { return workTypeId; }
    public void setWorkTypeId(Integer workTypeId) { this.workTypeId = workTypeId; }
    public Integer getCropId() { return cropId; }
    public void setCropId(Integer cropId) { this.cropId = cropId; }
    public String getAreaDesc() { return areaDesc; }
    public void setAreaDesc(String areaDesc) { this.areaDesc = areaDesc; }
    public String getLocationDesc() { return locationDesc; }
    public void setLocationDesc(String locationDesc) { this.locationDesc = locationDesc; }
    public String getPlotNotes() { return plotNotes; }
    public void setPlotNotes(String plotNotes) { this.plotNotes = plotNotes; }
    public String getPhotos() { return photos; }
    public void setPhotos(String photos) { this.photos = photos; }
    public String getExpectDateStart() { return expectDateStart; }
    public void setExpectDateStart(String expectDateStart) { this.expectDateStart = expectDateStart; }
    public String getExpectDateEnd() { return expectDateEnd; }
    public void setExpectDateEnd(String expectDateEnd) { this.expectDateEnd = expectDateEnd; }
}
