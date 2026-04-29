package com.nongzhushou.matchflow.model;

public class MatchTaskLite {
    private Long id;
    private Long demandId;
    private Integer currentTier;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
    public Integer getCurrentTier() { return currentTier; }
    public void setCurrentTier(Integer currentTier) { this.currentTier = currentTier; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
