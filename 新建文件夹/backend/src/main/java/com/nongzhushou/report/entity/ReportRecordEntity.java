package com.nongzhushou.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("report_record")
public class ReportRecordEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reporterId;
    private Long reportedUserId;
    private Long orderId;
    private Long demandId;
    private String reportType;
    private String content;
    private String evidenceAssetIds;
    private String status;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }
    public Long getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(Long reportedUserId) { this.reportedUserId = reportedUserId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getEvidenceAssetIds() { return evidenceAssetIds; }
    public void setEvidenceAssetIds(String evidenceAssetIds) { this.evidenceAssetIds = evidenceAssetIds; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
