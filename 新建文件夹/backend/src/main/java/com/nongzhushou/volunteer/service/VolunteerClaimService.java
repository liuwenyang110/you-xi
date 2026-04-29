package com.nongzhushou.volunteer.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nongzhushou.volunteer.entity.VolunteerClaimEntity;
import com.nongzhushou.volunteer.mapper.VolunteerClaimMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VolunteerClaimService {

    private final VolunteerClaimMapper claimMapper;

    public VolunteerClaimService(VolunteerClaimMapper claimMapper) {
        this.claimMapper = claimMapper;
    }

    /** 志愿认领一个需求 (非竞争性，先到先得但不排他——公益优先) */
    public Long claim(Long demandId, Long volunteerId, Long equipmentId) {
        // 检查是否已被认领
        LambdaQueryWrapper<VolunteerClaimEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(VolunteerClaimEntity::getDemandId, demandId);
        qw.in(VolunteerClaimEntity::getStatus, "CLAIMED", "EN_ROUTE", "WORKING");
        Long existingCount = claimMapper.selectCount(qw);
        if (existingCount > 0) {
            throw new RuntimeException("该需求已有志愿者认领，感谢您的热心！");
        }

        VolunteerClaimEntity claim = new VolunteerClaimEntity();
        claim.setDemandId(demandId);
        claim.setVolunteerId(volunteerId);
        claim.setEquipmentId(equipmentId);
        claim.setStatus("CLAIMED");
        claim.setClaimedAt(LocalDateTime.now());
        claimMapper.insert(claim);
        return claim.getId();
    }

    /** 更新状态：出发 -> 到达 -> 作业中 -> 完成 */
    public void updateStatus(Long claimId, String newStatus) {
        VolunteerClaimEntity claim = claimMapper.selectById(claimId);
        if (claim == null) throw new RuntimeException("认领记录不存在");

        claim.setStatus(newStatus);
        switch (newStatus) {
            case "EN_ROUTE": break;
            case "WORKING": claim.setArrivedAt(LocalDateTime.now()); break;
            case "FINISHED": claim.setFinishedAt(LocalDateTime.now()); break;
            case "CANCELLED": break;
        }
        claimMapper.updateById(claim);
    }

    /** 农户反馈评分 */
    public void feedback(Long claimId, String feedback, Integer rating) {
        VolunteerClaimEntity claim = claimMapper.selectById(claimId);
        if (claim == null) throw new RuntimeException("认领记录不存在");
        claim.setFarmerFeedback(feedback);
        claim.setFarmerRating(rating);
        claimMapper.updateById(claim);
    }

    /** 查询某志愿者的历史帮扶记录 */
    public List<VolunteerClaimEntity> listByVolunteer(Long volunteerId) {
        LambdaQueryWrapper<VolunteerClaimEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(VolunteerClaimEntity::getVolunteerId, volunteerId);
        qw.orderByDesc(VolunteerClaimEntity::getCreatedAt);
        return claimMapper.selectList(qw);
    }

    /** 查询某需求的认领状态 */
    public VolunteerClaimEntity getByDemand(Long demandId) {
        LambdaQueryWrapper<VolunteerClaimEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(VolunteerClaimEntity::getDemandId, demandId);
        qw.ne(VolunteerClaimEntity::getStatus, "CANCELLED");
        qw.last("LIMIT 1");
        return claimMapper.selectOne(qw);
    }
}
