package com.nongzhushou.match.model;

public class OwnerCandidate {
    private Long ownerId;
    private Long serviceItemId;
    private Double distanceKm;
    private Double straightDistanceKm;
    private Long durationMinutes;
    private String distanceSource;
    private Integer score;
    private String reason;

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Long getServiceItemId() { return serviceItemId; }
    public void setServiceItemId(Long serviceItemId) { this.serviceItemId = serviceItemId; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public Double getStraightDistanceKm() { return straightDistanceKm; }
    public void setStraightDistanceKm(Double straightDistanceKm) { this.straightDistanceKm = straightDistanceKm; }
    public Long getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Long durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getDistanceSource() { return distanceSource; }
    public void setDistanceSource(String distanceSource) { this.distanceSource = distanceSource; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
