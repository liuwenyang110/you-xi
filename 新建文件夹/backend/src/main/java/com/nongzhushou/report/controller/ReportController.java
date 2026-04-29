package com.nongzhushou.report.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.report.dto.ReportCreateRequest;
import com.nongzhushou.report.service.ReportService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody ReportCreateRequest request) {
        return Result.ok(reportService.create(request));
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(reportService.list());
    }
}
