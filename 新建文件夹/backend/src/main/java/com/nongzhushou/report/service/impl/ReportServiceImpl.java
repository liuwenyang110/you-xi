package com.nongzhushou.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nongzhushou.admin.entity.AdminAuditEntity;
import com.nongzhushou.admin.mapper.AdminAuditMapper;
import com.nongzhushou.report.entity.ReportRecordEntity;
import com.nongzhushou.report.mapper.ReportRecordMapper;
import com.nongzhushou.report.dto.ReportCreateRequest;
import com.nongzhushou.report.service.ReportService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRecordMapper reportRecordMapper;
    private final AdminAuditMapper adminAuditMapper;

    public ReportServiceImpl(ReportRecordMapper reportRecordMapper, AdminAuditMapper adminAuditMapper) {
        this.reportRecordMapper = reportRecordMapper;
        this.adminAuditMapper = adminAuditMapper;
    }

    @Override
    public Long create(ReportCreateRequest request) {
        ReportRecordEntity entity = new ReportRecordEntity();
        entity.setReporterId(10001L);
        entity.setReportedUserId(request.getReportedUserId());
        entity.setOrderId(request.getOrderId());
        entity.setDemandId(request.getDemandId());
        entity.setReportType(request.getReportType());
        entity.setContent(request.getContent());
        entity.setEvidenceAssetIds(request.getEvidenceAssetIds() == null || request.getEvidenceAssetIds().isEmpty()
                ? null
                : request.getEvidenceAssetIds().stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(null));
        entity.setStatus("PENDING");
        reportRecordMapper.insert(entity);

        AdminAuditEntity audit = new AdminAuditEntity();
        audit.setAction("create_report");
        audit.setContent("Create report " + entity.getId());
        adminAuditMapper.insert(audit);
        return entity.getId();
    }

    @Override
    public List<Map<String, Object>> list() {
        return reportRecordMapper.selectList(new LambdaQueryWrapper<ReportRecordEntity>().orderByDesc(ReportRecordEntity::getId))
                .stream()
                .map(this::toMap)
                .toList();
    }

    private Map<String, Object> toMap(ReportRecordEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("reporterId", entity.getReporterId());
        map.put("reportedUserId", entity.getReportedUserId());
        map.put("orderId", entity.getOrderId());
        map.put("demandId", entity.getDemandId());
        map.put("reportType", entity.getReportType());
        map.put("content", entity.getContent());
        map.put("evidenceAssetIds", entity.getEvidenceAssetIds());
        map.put("status", entity.getStatus());
        map.put("createdAt", entity.getCreatedAt());
        return map;
    }
}
