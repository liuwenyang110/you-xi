package com.nongzhushou.volunteer.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.volunteer.entity.VolunteerClaimEntity;
import com.nongzhushou.volunteer.service.VolunteerClaimService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v3/volunteer")
public class VolunteerController {

    private final VolunteerClaimService claimService;

    public VolunteerController(VolunteerClaimService claimService) {
        this.claimService = claimService;
    }

    /** 志愿认领帮扶 (机主端滑动认领后调用) */
    @PostMapping("/claim")
    public Result<Long> claim(@RequestBody Map<String, Long> body) {
        Long demandId = body.get("demandId");
        Long volunteerId = body.get("volunteerId");
        Long equipmentId = body.get("equipmentId");
        return Result.ok(claimService.claim(demandId, volunteerId, equipmentId));
    }

    /** 更新认领状态 (EN_ROUTE / WORKING / FINISHED / CANCELLED) */
    @PutMapping("/claim/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam String status) {
        claimService.updateStatus(id, status);
        return Result.ok("状态已更新为: " + status);
    }

    /** 农户评价反馈 */
    @PostMapping("/claim/{id}/feedback")
    public Result<String> feedback(@PathVariable Long id,
                                   @RequestParam String feedback,
                                   @RequestParam Integer rating) {
        claimService.feedback(id, feedback, rating);
        return Result.ok("感谢评价");
    }

    /** 志愿者历史帮扶记录 */
    @GetMapping("/history")
    public Result<List<VolunteerClaimEntity>> history(@RequestParam Long volunteerId) {
        return Result.ok(claimService.listByVolunteer(volunteerId));
    }

    /** 查询某需求的认领状态 */
    @GetMapping("/claim/by-demand/{demandId}")
    public Result<VolunteerClaimEntity> claimByDemand(@PathVariable Long demandId) {
        return Result.ok(claimService.getByDemand(demandId));
    }
}
