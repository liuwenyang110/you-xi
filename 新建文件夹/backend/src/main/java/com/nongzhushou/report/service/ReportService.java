package com.nongzhushou.report.service;

import com.nongzhushou.report.dto.ReportCreateRequest;
import java.util.List;
import java.util.Map;

public interface ReportService {

    Long create(ReportCreateRequest request);

    List<Map<String, Object>> list();
}
