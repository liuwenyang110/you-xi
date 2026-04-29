package com.nongzhushou.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class ReportCreateRequest {

    private Long orderId;
    private Long demandId;

    @NotNull
    @Positive(message = "reportedUserId must be greater than 0")
    private Long reportedUserId;

    @NotBlank
    private String reportType;

    @NotBlank
    @Size(min = 5, max = 500, message = "content length must be between 5 and 500")
    private String content;

    private List<Long> evidenceAssetIds = new ArrayList<>();

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getDemandId() {
        return demandId;
    }

    public void setDemandId(Long demandId) {
        this.demandId = demandId;
    }

    public Long getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(Long reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Long> getEvidenceAssetIds() {
        return evidenceAssetIds;
    }

    public void setEvidenceAssetIds(List<Long> evidenceAssetIds) {
        this.evidenceAssetIds = evidenceAssetIds == null ? new ArrayList<>() : evidenceAssetIds;
    }
}
