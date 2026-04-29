package com.nongzhushou.v3demand.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.v3demand.dto.DemandGroupCreateDTO;
import com.nongzhushou.v3demand.dto.DemandPublishDTO;
import com.nongzhushou.v3demand.entity.V3DemandEntity;
import com.nongzhushou.v3demand.entity.V3DemandGroupEntity;
import com.nongzhushou.v3demand.entity.V3GroupMemberEntity;
import com.nongzhushou.v3demand.service.V3DemandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v3")
public class V3DemandController {

    @Autowired
    private V3DemandService demandService;

    // ===== 个人需求 =====

    /** POST /api/v3/demands — 发布个人需求 */
    @PostMapping("/demands")
    public Result<V3DemandEntity> publish(@RequestBody DemandPublishDTO params) {
        return Result.ok(demandService.publish(params));
    }

    /** GET /api/v3/demands/my — 我的需求列表 */
    @GetMapping("/demands/my")
    public Result<List<V3DemandEntity>> myDemands() {
        return Result.ok(demandService.myDemands());
    }

    /** PUT /api/v3/demands/{id}/status — 更新需求状态 */
    @PutMapping("/demands/{id}/status")
    public Result<V3DemandEntity> updateStatus(@PathVariable Long id,
                                                @RequestParam String status) {
        return Result.ok(demandService.updateStatus(id, status));
    }

    /** GET /api/v3/zones/{zoneId}/demands — 片区需求列表（农机手查看） */
    @GetMapping("/zones/{zoneId}/demands")
    public Result<Map<String, Object>> getZoneDemands(@PathVariable Long zoneId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return Result.ok(demandService.getZoneDemands(zoneId, page, size));
    }

    // ===== 合集需求 =====

    /** POST /api/v3/demand-groups — 发起合集需求 */
    @PostMapping("/demand-groups")
    public Result<V3DemandGroupEntity> createGroup(@RequestBody DemandGroupCreateDTO params) {
        return Result.ok(demandService.createGroup(params));
    }

    /** GET /api/v3/zones/{zoneId}/demand-groups — 片区合集需求列表 */
    @GetMapping("/zones/{zoneId}/demand-groups")
    public Result<List<V3DemandGroupEntity>> getZoneGroups(@PathVariable Long zoneId) {
        return Result.ok(demandService.getZoneGroups(zoneId));
    }

    /** GET /api/v3/demand-groups/{id} — 合集需求详情 */
    @GetMapping("/demand-groups/{id}")
    public Result<Map<String, Object>> getGroupDetail(@PathVariable Long id) {
        return Result.ok(demandService.getGroupDetail(id));
    }

    /** POST /api/v3/demand-groups/{id}/join — 申请加入合集 */
    @PostMapping("/demand-groups/{id}/join")
    public Result<V3GroupMemberEntity> applyJoin(@PathVariable Long id,
                                                  @RequestBody DemandPublishDTO params) {
        // 申请加入时使用的字段与发布个人需求相似，复用 DTO 或按需拆分
        return Result.ok(demandService.applyJoinGroup(id, params));
    }

    /** PUT /api/v3/demand-groups/members/{memberId}/approve — 审批加入申请 */
    @PutMapping("/demand-groups/members/{memberId}/approve")
    public Result<V3GroupMemberEntity> approveJoin(@PathVariable Long memberId,
                                                    @RequestParam boolean approved) {
        return Result.ok(demandService.approveJoin(memberId, approved));
    }
}
