package com.nongzhushou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nongzhushou.admin.entity.AdminAuditEntity;
import com.nongzhushou.admin.mapper.AdminAuditMapper;
import com.nongzhushou.admin.service.AdminService;
import com.nongzhushou.collab.mapper.CollabMessageMapper;
import com.nongzhushou.collab.mapper.CollabSessionEventMapper;
import com.nongzhushou.common.enums.DemandStatus;
import com.nongzhushou.contact.entity.ContactSessionEntity;
import com.nongzhushou.contact.mapper.ContactSessionMapper;
import com.nongzhushou.demand.entity.DemandEntity;
import com.nongzhushou.demand.mapper.DemandMapper;
import com.nongzhushou.equipment.entity.EquipmentEntity;
import com.nongzhushou.equipment.mapper.EquipmentMapper;
import com.nongzhushou.match.entity.MatchAttemptEntity;
import com.nongzhushou.match.mapper.MatchAttemptMapper;
import com.nongzhushou.order.entity.OrderInfoEntity;
import com.nongzhushou.order.mapper.OrderInfoMapper;
import com.nongzhushou.report.entity.ReportRecordEntity;
import com.nongzhushou.report.mapper.ReportRecordMapper;
import com.nongzhushou.serviceitem.mapper.ServiceItemMapper;
import com.nongzhushou.user.entity.UserAccountEntity;
import com.nongzhushou.user.mapper.UserAccountMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    private final DemandMapper demandMapper;
    private final EquipmentMapper equipmentMapper;
    private final UserAccountMapper userAccountMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final MatchAttemptMapper matchAttemptMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final ReportRecordMapper reportRecordMapper;
    private final AdminAuditMapper adminAuditMapper;
    private final ContactSessionMapper contactSessionMapper;
    private final CollabMessageMapper collabMessageMapper;
    private final CollabSessionEventMapper collabSessionEventMapper;

    public AdminServiceImpl(
            DemandMapper demandMapper,
            EquipmentMapper equipmentMapper,
            UserAccountMapper userAccountMapper,
            ServiceItemMapper serviceItemMapper,
            MatchAttemptMapper matchAttemptMapper,
            OrderInfoMapper orderInfoMapper,
            ReportRecordMapper reportRecordMapper,
            AdminAuditMapper adminAuditMapper,
            ContactSessionMapper contactSessionMapper,
            CollabMessageMapper collabMessageMapper,
            CollabSessionEventMapper collabSessionEventMapper
    ) {
        this.demandMapper = demandMapper;
        this.equipmentMapper = equipmentMapper;
        this.userAccountMapper = userAccountMapper;
        this.serviceItemMapper = serviceItemMapper;
        this.matchAttemptMapper = matchAttemptMapper;
        this.orderInfoMapper = orderInfoMapper;
        this.reportRecordMapper = reportRecordMapper;
        this.adminAuditMapper = adminAuditMapper;
        this.contactSessionMapper = contactSessionMapper;
        this.collabMessageMapper = collabMessageMapper;
        this.collabSessionEventMapper = collabSessionEventMapper;
    }

    @Override
    public Map<String, Object> dashboard() {
        long demandWithCoords = demandMapper.selectCount(new LambdaQueryWrapper<DemandEntity>()
                .isNotNull(DemandEntity::getLat)
                .isNotNull(DemandEntity::getLng));
        long equipmentWithCoords = equipmentMapper.selectCount(new LambdaQueryWrapper<EquipmentEntity>()
                .isNotNull(EquipmentEntity::getCurrentLat)
                .isNotNull(EquipmentEntity::getCurrentLng));

        long farmerCount = userAccountMapper.selectCount(new LambdaQueryWrapper<UserAccountEntity>().eq(UserAccountEntity::getRole, "FARMER"));
        long operatorCount = userAccountMapper.selectCount(new LambdaQueryWrapper<UserAccountEntity>().eq(UserAccountEntity::getRole, "OPERATOR"));
        long zoneCount = zoneInfoMapper.selectCount(null);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("overview", Map.of(
                "demandCount", demandCount,
                "equipmentCount", equipmentCount,
                "farmerCount", farmerCount,
                "operatorCount", operatorCount,
                "zoneCount", zoneCount,
                "machineryCount", equipmentCount,
                "totalUserCount", farmerCount + operatorCount
        ));
        map.put("demandStats", Map.of(
                "pendingCount", demandMapper.selectCount(new LambdaQueryWrapper<DemandEntity>().eq(DemandEntity::getStatus, "PUBLISHED")),
                "contactedCount", demandMapper.selectCount(new LambdaQueryWrapper<DemandEntity>().eq(DemandEntity::getStatus, "CONTACTED")),
                "doneCount", demandMapper.selectCount(new LambdaQueryWrapper<DemandEntity>().eq(DemandEntity::getStatus, "COMPLETED")),
                "cancelledCount", demandMapper.selectCount(new LambdaQueryWrapper<DemandEntity>().eq(DemandEntity::getStatus, "CANCELLED"))
        ));
        map.put("helpSuccessCount", demandMapper.selectCount(new LambdaQueryWrapper<DemandEntity>().eq(DemandEntity::getStatus, "COMPLETED")));
        map.put("reportCount", reportRecordMapper.selectCount(null));
        map.putAll(collabSummary());
        return map;
    }

    @Override
    public Map<String, Object> collabSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("collabSessionCount", contactSessionMapper.selectCount(null));
        summary.put("collabMessageCount", collabMessageMapper.selectCount(null));
        summary.put("collabEventCount", collabSessionEventMapper.selectCount(null));
        summary.put("highRiskSessionCount", contactSessionMapper.selectCount(new LambdaQueryWrapper<ContactSessionEntity>()
                .ge(ContactSessionEntity::getUnreadFarmerCount, 5)
                .or()
                .ge(ContactSessionEntity::getUnreadOwnerCount, 5)));
        return summary;
    }

    private double percent(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0D;
        }
        return Math.round((numerator * 1000D / denominator)) / 10D;
    }

    @Override
    public List<Map<String, Object>> matchTasks() {
        return List.of(); // 已弃用撮合任务，全面转为社区/志愿者认领模式
    }

    @Override
    public List<Map<String, Object>> demands() {
        return demandMapper.selectList(new LambdaQueryWrapper<DemandEntity>().orderByDesc(DemandEntity::getId))
                .stream()
                .map(item -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", item.getId());
                    map.put("farmerId", item.getFarmerId());
                    map.put("villageName", item.getVillageName());
                    map.put("cropCode", item.getCropCode());
                    map.put("areaMu", item.getAreaMu());
                    map.put("scheduleType", item.getScheduleType());
                    map.put("expectedDate", item.getExpectedDate());
                    map.put("remark", item.getRemark());
                    map.put("status", item.getStatus());
                    map.put("statusLabel", DemandStatus.labelOf(item.getStatus()));
                    map.put("statusDescription", DemandStatus.descriptionOf(item.getStatus()));
                    map.put("createdAt", item.getCreatedAt());
                    map.put("publishedAt", item.getPublishedAt());
                    return map;
                })
                .toList();
    }

    @Override
    public List<Map<String, Object>> orders() {
        return orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfoEntity>().orderByDesc(OrderInfoEntity::getId))
                .stream()
                .map(item -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", item.getId());
                    map.put("demandId", item.getDemandId());
                    map.put("matchAttemptId", item.getMatchAttemptId());
                    map.put("contactSessionId", item.getContactSessionId());
                    map.put("ownerId", item.getOwnerId());
                    map.put("farmerId", item.getFarmerId());
                    map.put("serviceItemId", item.getServiceItemId());
                    map.put("status", item.getStatus());
                    map.put("statusLabel", com.nongzhushou.common.enums.OrderStatus.labelOf(item.getStatus()));
                    map.put("statusDescription", com.nongzhushou.common.enums.OrderStatus.descriptionOf(item.getStatus()));
                    map.put("ownerConfirmedFinish", item.getOwnerConfirmedFinish());
                    map.put("farmerConfirmedFinish", item.getFarmerConfirmedFinish());
                    map.put("contactConfirmedAt", item.getContactConfirmedAt());
                    map.put("createdAt", item.getCreatedAt());
                    return map;
                })
                .toList();
    }

    @Override
    public List<Map<String, Object>> users() {
        return userAccountMapper.selectList(new LambdaQueryWrapper<UserAccountEntity>().orderByDesc(UserAccountEntity::getId))
                .stream()
                .map(item -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", item.getId());
                    map.put("phone", item.getPhone());
                    map.put("primaryRole", item.getPrimaryRole());
                    map.put("currentRole", item.getCurrentRole());
                    map.put("status", item.getStatus());
                    map.put("createdAt", item.getCreatedAt());
                    map.put("updatedAt", item.getUpdatedAt());
                    return map;
                })
                .toList();
    }

    @Override
    public List<Map<String, Object>> equipment() {
        return equipmentMapper.selectList(new LambdaQueryWrapper<EquipmentEntity>().orderByDesc(EquipmentEntity::getId))
                .stream()
                .map(item -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", item.getId());
                    map.put("ownerId", item.getOwnerId());
                    map.put("machineTypeId", item.getMachineTypeId());
                    map.put("equipmentName", item.getEquipmentName());
                    map.put("brandModel", item.getBrandModel());
                    map.put("quantity", item.getQuantity());
                    map.put("currentStatus", item.getCurrentStatus());
                    map.put("baseRegionCode", item.getBaseRegionCode());
                    map.put("serviceRadiusKm", item.getServiceRadiusKm());
                    map.put("currentLat", item.getCurrentLat());
                    map.put("currentLng", item.getCurrentLng());
                    map.put("approveStatus", item.getApproveStatus());
                    map.put("createdAt", item.getCreatedAt());
                    return map;
                })
                .toList();
    }

    @Override
    public List<Map<String, Object>> reports() {
        return reportRecordMapper.selectList(new LambdaQueryWrapper<ReportRecordEntity>().orderByDesc(ReportRecordEntity::getId))
                .stream()
                .map(item -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", item.getId());
                    map.put("reportedUserId", item.getReportedUserId());
                    map.put("reportType", item.getReportType());
                    map.put("status", item.getStatus());
                    return map;
                })
                .toList();
    }

    @Override
    public List<Map<String, Object>> audits() {
        return adminAuditMapper.selectList(new LambdaQueryWrapper<AdminAuditEntity>().orderByDesc(AdminAuditEntity::getId))
                .stream()
                .map(item -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", item.getId());
                    map.put("action", item.getAction());
                    map.put("content", item.getContent());
                    map.put("createdAt", item.getCreatedAt());
                    return map;
                })
                .toList();
    }

    @Override
    public List<Map<String, Object>> collabSessions() {
        return contactSessionMapper.selectList(new LambdaQueryWrapper<ContactSessionEntity>()
                        .orderByDesc(ContactSessionEntity::getLastMessageAt)
                        .orderByDesc(ContactSessionEntity::getId))
                .stream()
                .map(item -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", item.getId());
                    map.put("subject", item.getSubject());
                    map.put("status", item.getStatus());
                    map.put("sessionType", item.getSessionType());
                    map.put("summary", item.getSummary());
                    map.put("lastMessagePreview", item.getLastMessagePreview());
                    map.put("lastMessageAt", item.getLastMessageAt());
                    map.put("unreadFarmerCount", item.getUnreadFarmerCount());
                    map.put("unreadOwnerCount", item.getUnreadOwnerCount());
                    return map;
                })
                .toList();
    }
}
