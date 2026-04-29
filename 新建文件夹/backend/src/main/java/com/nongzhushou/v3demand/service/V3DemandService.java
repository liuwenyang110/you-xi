package com.nongzhushou.v3demand.service;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.common.exception.BizException;
import com.nongzhushou.common.exception.ErrorCode;
import com.nongzhushou.v3demand.entity.*;
import com.nongzhushou.v3demand.dto.*;
import com.nongzhushou.v3demand.mapper.*;
import com.nongzhushou.v3user.entity.V3UserEntity;
import com.nongzhushou.v3user.service.V3UserService;
import com.nongzhushou.teahouse.service.TeahouseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class V3DemandService {
    private static final Logger log = LoggerFactory.getLogger(V3DemandService.class);

    @Autowired private V3DemandMapper demandMapper;
    @Autowired private V3DemandGroupMapper groupMapper;
    @Autowired private V3GroupMemberMapper memberMapper;
    @Autowired private V3UserService v3UserService;
    @Autowired private TeahouseService teahouseService;

    // ============ 个人需求 ============

    /**
     * 农户发布个人需求
     */
    @Transactional
    public V3DemandEntity publish(DemandPublishDTO params) {
        V3UserEntity farmer = v3UserService.getCurrentV3User();
        log.info("用户ID[{}]发布需求：{}", farmer.getId(), params.getPlotNotes());
        
        if (!"FARMER".equals(farmer.getRole())) {
            throw new BizException(ErrorCode.BIZ_ERROR, "仅农户可发布需求");
        }
        if (farmer.getZoneId() == null) {
            throw new BizException(ErrorCode.BIZ_ERROR, "请先加入片区再发布需求");
        }

        V3DemandEntity demand = new V3DemandEntity();
        demand.setFarmerId(farmer.getId());
        demand.setZoneId(farmer.getZoneId());
        demand.setWorkTypeId(params.getWorkTypeId());
        demand.setCropId(params.getCropId());
        demand.setAreaDesc(params.getAreaDesc());
        demand.setLocationDesc(params.getLocationDesc());
        demand.setPlotNotes(params.getPlotNotes());
        demand.setPhotos(params.getPhotos());
        // 期望时间
        if (params.getExpectDateStart() != null) {
            demand.setExpectDateStart(LocalDate.parse(params.getExpectDateStart()));
        }
        if (params.getExpectDateEnd() != null) {
            demand.setExpectDateEnd(LocalDate.parse(params.getExpectDateEnd()));
        }
        demand.setStatus("PUBLISHED");
        demand.setPublishedAt(LocalDateTime.now());
        demand.setCreatedAt(LocalDateTime.now());
        demand.setUpdatedAt(LocalDateTime.now());
        demand.setAutoCleaned(0);
        demandMapper.insert(demand);
        // 联动茶馆：发布需求时自动开张/刷新
        teahouseService.onDemandPublished(farmer.getZoneId(), farmer.getId());
        return demand;
    }

    /**
     * 更新需求状态（农户手动操作）
     */
    @Transactional
    public V3DemandEntity updateStatus(Long demandId, String status) {
        V3UserEntity farmer = v3UserService.getCurrentV3User();
        V3DemandEntity demand = demandMapper.selectById(demandId);
        if (demand == null) throw new BizException(ErrorCode.BIZ_ERROR, "需求不存在");
        if (!demand.getFarmerId().equals(farmer.getId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权操作他人的需求");
        }
        // 合法状态流转检查
        List<String> validStatuses = Arrays.asList("CONTACTED", "COMPLETED", "CANCELLED");
        if (!validStatuses.contains(status)) {
            throw new BizException(ErrorCode.BIZ_ERROR, "无效的状态");
        }
        demand.setStatus(status);
        demand.setUpdatedAt(LocalDateTime.now());
        demandMapper.updateById(demand);
        return demand;
    }

    /**
     * 我的需求列表（农户）
     */
    public List<V3DemandEntity> myDemands() {
        V3UserEntity farmer = v3UserService.getCurrentV3User();
        return demandMapper.findByFarmer(farmer.getId());
    }

    /**
     * 片区需求列表（农机手查看）
     */
    public Map<String, Object> getZoneDemands(Long zoneId, int page, int size) {
        V3UserEntity user = v3UserService.getCurrentV3User();
        // 验证用户在本片区
        if (!zoneId.equals(user.getZoneId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "仅可查看本片区的需求");
        }
        int offset = (page - 1) * size;
        List<V3DemandEntity> demands = demandMapper.findActiveByZone(zoneId, size, offset);
        int total = demandMapper.countActiveByZone(zoneId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", demands);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    // ============ 合集需求 ============

    /**
     * 发起合集需求
     */
    @Transactional
    public V3DemandGroupEntity createGroup(DemandGroupCreateDTO params) {
        V3UserEntity farmer = v3UserService.getCurrentV3User();
        log.info("用户ID[{}]发起合集需求：{}", farmer.getId(), params.getTitle());
        
        if (!"FARMER".equals(farmer.getRole())) {
            throw new BizException(ErrorCode.BIZ_ERROR, "仅农户可发起合集需求");
        }
        if (farmer.getZoneId() == null) {
            throw new BizException(ErrorCode.BIZ_ERROR, "请先加入片区");
        }

        V3DemandGroupEntity group = new V3DemandGroupEntity();
        group.setZoneId(farmer.getZoneId());
        group.setCreatorId(farmer.getId());
        group.setWorkTypeId(params.getWorkTypeId());
        group.setCropId(params.getCropId());
        group.setTitle(params.getTitle());
        group.setTotalAreaDesc(params.getTotalAreaDesc());
        group.setLocationDesc(params.getLocationDesc());
        if (params.getExpectDateStart() != null) {
            group.setExpectDateStart(LocalDate.parse(params.getExpectDateStart()));
        }
        if (params.getExpectDateEnd() != null) {
            group.setExpectDateEnd(LocalDate.parse(params.getExpectDateEnd()));
        }
        group.setMemberCount(1);
        group.setStatus("OPEN");
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        groupMapper.insert(group);

        // 自动将发起人加入成员（已批准状态）
        V3GroupMemberEntity creator = new V3GroupMemberEntity();
        creator.setGroupId(group.getId());
        creator.setFarmerId(farmer.getId());
        creator.setAreaDesc(params.getMyAreaDesc());
        creator.setLocationDesc(params.getMyLocationDesc());
        creator.setJoinStatus("APPROVED");
        creator.setApprovedBy(farmer.getId());
        creator.setApprovedAt(LocalDateTime.now());
        creator.setCreatedAt(LocalDateTime.now());
        memberMapper.insert(creator);

        return group;
    }

    /**
     * 申请加入合集需求
     */
    @Transactional
    public V3GroupMemberEntity applyJoinGroup(Long groupId, DemandPublishDTO params) {
        V3UserEntity farmer = v3UserService.getCurrentV3User();
        V3DemandGroupEntity group = groupMapper.selectById(groupId);
        if (group == null) {
             throw new BizException(ErrorCode.BIZ_ERROR, "合集需求不存在");
        }
        
        log.info("用户ID[{}]申请加入合集需求[{}]", farmer.getId(), groupId);
        
        if (!"OPEN".equals(group.getStatus())) {
            throw new BizException(ErrorCode.BIZ_ERROR, "合集需求已关闭");
        }
        // 必须是同片区
        if (!group.getZoneId().equals(farmer.getZoneId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "只能申请加入本片区的合集需求");
        }
        // 检查是否已申请
        V3GroupMemberEntity existing = memberMapper.findByGroupAndFarmer(groupId, farmer.getId());
        if (existing != null) {
            throw new BizException(ErrorCode.BIZ_ERROR, "您已申请过该合集需求");
        }

        V3GroupMemberEntity member = new V3GroupMemberEntity();
        member.setGroupId(groupId);
        member.setFarmerId(farmer.getId());
        member.setAreaDesc(params.getAreaDesc());
        member.setLocationDesc(params.getLocationDesc());
        member.setJoinStatus("PENDING");
        member.setCreatedAt(LocalDateTime.now());
        memberMapper.insert(member);
        return member;
    }

    /**
     * 审批加入申请（发起人操作）
     */
    @Transactional
    public V3GroupMemberEntity approveJoin(Long memberId, boolean approved) {
        V3UserEntity farmer = v3UserService.getCurrentV3User();
        V3GroupMemberEntity member = memberMapper.selectById(memberId);
        if (member == null) throw new BizException(ErrorCode.BIZ_ERROR, "申请记录不存在");

        V3DemandGroupEntity group = groupMapper.selectById(member.getGroupId());
        if (!group.getCreatorId().equals(farmer.getId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "仅发起人可审批加入申请");
        }

        member.setJoinStatus(approved ? "APPROVED" : "REJECTED");
        member.setApprovedBy(farmer.getId());
        member.setApprovedAt(LocalDateTime.now());
        memberMapper.updateById(member);

        if (approved) {
            // 更新合集成员数
            group.setMemberCount(memberMapper.findApprovedByGroup(group.getId()).size());
            group.setUpdatedAt(LocalDateTime.now());
            groupMapper.updateById(group);
        }
        return member;
    }

    /**
     * 获取片区内开放的合集需求列表
     */
    public List<V3DemandGroupEntity> getZoneGroups(Long zoneId) {
        return groupMapper.findOpenByZone(zoneId);
    }

    /**
     * 合集需求详情（含成员列表）
     */
    public Map<String, Object> getGroupDetail(Long groupId) {
        V3DemandGroupEntity group = groupMapper.selectById(groupId);
        if (group == null) throw new BizException(ErrorCode.BIZ_ERROR, "合集需求不存在");
        List<V3GroupMemberEntity> members = memberMapper.findApprovedByGroup(groupId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("group", group);
        result.put("members", members);
        return result;
    }

    // ---- 工具方法 ----
    private String str(Object o) { return o == null ? null : o.toString(); }
    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Integer) return (Integer) o;
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return null; }
    }
}
