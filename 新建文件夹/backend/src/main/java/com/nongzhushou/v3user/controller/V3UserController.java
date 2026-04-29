package com.nongzhushou.v3user.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.v3user.dto.V3JoinZoneRequest;
import com.nongzhushou.v3user.dto.V3SelectRoleRequest;
import com.nongzhushou.v3user.entity.V3UserEntity;
import com.nongzhushou.v3user.service.V3UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v3")
public class V3UserController {

    @Autowired
    private V3UserService v3UserService;

    /**
     * 选择角色（一次性，不可更改）
     * POST /api/v3/user/role
     */
    @PostMapping("/user/role")
    public Result<V3UserEntity> selectRole(@Valid @RequestBody V3SelectRoleRequest req) {
        return Result.ok(v3UserService.selectRole(req));
    }

    /**
     * 加入/切换片区
     * POST /api/v3/user/zone
     */
    @PostMapping("/user/zone")
    public Result<V3UserEntity> joinZone(@Valid @RequestBody V3JoinZoneRequest req) {
        return Result.ok(v3UserService.joinZone(req));
    }

    /**
     * 获取当前用户 V3 档案
     * GET /api/v3/user/profile
     */
    @GetMapping("/user/profile")
    public Result<Map<String, Object>> getProfile() {
        return Result.ok(v3UserService.getProfile());
    }

    /**
     * 获取本片区农机手列表（仅本片区农户可调用）
     * GET /api/v3/zones/{zoneId}/operators
     */
    @GetMapping("/zones/{zoneId}/operators")
    public Result<List<V3UserEntity>> getZoneOperators(@PathVariable Long zoneId) {
        return Result.ok(v3UserService.getZoneOperators(zoneId));
    }

    // ===== 管理员接口 =====

    /**
     * 管理员查询用户列表（按角色分页）
     * GET /api/v3/admin/users?role=FARMER&size=100
     */
    @GetMapping("/admin/users")
    public Result<Map<String, Object>> adminListUsers(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        return Result.ok(v3UserService.adminListUsers(role, page, size));
    }

    /**
     * 管理员修改用户状态
     * PUT /api/v3/admin/users/{userId}/status
     */
    @PutMapping("/admin/users/{userId}/status")
    public Result<Void> adminUpdateUserStatus(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        v3UserService.adminUpdateUserStatus(userId, body.get("status"));
        return Result.ok(null);
    }

    /**
     * 管理员数据看板
     * GET /api/v3/admin/dashboard
     */
    @GetMapping("/admin/dashboard")
    public Result<Map<String, Object>> adminDashboard() {
        return Result.ok(v3UserService.getDashboard());
    }

    /**
     * 管理员查询所有需求（分页）
     * GET /api/v3/admin/demands?page=1&size=20
     */
    @GetMapping("/admin/demands")
    public Result<Map<String, Object>> adminListDemands(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(v3UserService.adminListDemands(page, size));
    }
}
