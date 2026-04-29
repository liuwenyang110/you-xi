package com.nongzhushou.admin.controller;

import com.nongzhushou.admin.service.AdminService;
import com.nongzhushou.common.api.Result;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.ok(adminService.dashboard());
    }

    @GetMapping("/match-tasks")
    public Result<List<Map<String, Object>>> matchTasks() {
        return Result.ok(adminService.matchTasks());
    }

    @GetMapping("/demands")
    public Result<List<Map<String, Object>>> demands() {
        return Result.ok(adminService.demands());
    }

    @GetMapping("/orders")
    public Result<List<Map<String, Object>>> orders() {
        return Result.ok(adminService.orders());
    }

    @GetMapping("/users")
    public Result<List<Map<String, Object>>> users() {
        return Result.ok(adminService.users());
    }

    @GetMapping("/equipment")
    public Result<List<Map<String, Object>>> equipment() {
        return Result.ok(adminService.equipment());
    }

    @GetMapping("/reports")
    public Result<List<Map<String, Object>>> reports() {
        return Result.ok(adminService.reports());
    }

    @GetMapping("/audits")
    public Result<List<Map<String, Object>>> audits() {
        return Result.ok(adminService.audits());
    }

    @GetMapping("/collab/summary")
    public Result<Map<String, Object>> collabSummary() {
        return Result.ok(adminService.collabSummary());
    }

    @GetMapping("/collab/sessions")
    public Result<List<Map<String, Object>>> collabSessions() {
        return Result.ok(adminService.collabSessions());
    }
}
