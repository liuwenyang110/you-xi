package com.nongzhushou.contact.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nongzhushou.common.enums.ContactSessionStatus;
import com.nongzhushou.common.exception.BizException;
import com.nongzhushou.common.exception.ErrorCode;
import com.nongzhushou.common.security.AuthContext;
import com.nongzhushou.contact.entity.ContactSessionEntity;
import com.nongzhushou.contact.mapper.ContactSessionMapper;
import com.nongzhushou.contact.service.ContactService;
import com.nongzhushou.demand.entity.DemandEntity;
import com.nongzhushou.demand.mapper.DemandMapper;
import com.nongzhushou.match.entity.MatchAttemptEntity;
import com.nongzhushou.match.mapper.MatchAttemptMapper;
import com.nongzhushou.matchflow.service.MatchFlowService;
import com.nongzhushou.order.entity.OrderInfoEntity;
import com.nongzhushou.order.mapper.OrderInfoMapper;
import com.nongzhushou.user.entity.UserAccountEntity;
import com.nongzhushou.user.mapper.UserAccountMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactServiceImpl implements ContactService {
    private static final Logger log = LoggerFactory.getLogger(ContactServiceImpl.class);

    private final ContactSessionMapper contactSessionMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final DemandMapper demandMapper;
    private final MatchFlowService matchFlowService;
    private final MatchAttemptMapper matchAttemptMapper;
    private final UserAccountMapper userAccountMapper;

    public ContactServiceImpl(
            ContactSessionMapper contactSessionMapper,
            OrderInfoMapper orderInfoMapper,
            DemandMapper demandMapper,
            MatchFlowService matchFlowService,
            MatchAttemptMapper matchAttemptMapper,
            UserAccountMapper userAccountMapper
    ) {
        this.contactSessionMapper = contactSessionMapper;
        this.orderInfoMapper = orderInfoMapper;
        this.demandMapper = demandMapper;
        this.matchFlowService = matchFlowService;
        this.matchAttemptMapper = matchAttemptMapper;
        this.userAccountMapper = userAccountMapper;
    }

    @Override
    public List<Map<String, Object>> list() {
        Long currentUserId = AuthContext.currentUserId();
        return contactSessionMapper.selectList(new LambdaQueryWrapper<ContactSessionEntity>()
                        .eq(ContactSessionEntity::getFarmerId, currentUserId)
                        .orderByDesc(ContactSessionEntity::getId))
                .stream()
                .map(this::toMap)
                .toList();
    }

    @Override
    public Map<String, Object> confirm(Long id) {
        ContactSessionEntity contact = contactSessionMapper.selectById(id);
        if (contact == null) {
            log.warn("确认联系失败：会话ID[{}]不存在", id);
            throw new BizException(ErrorCode.NOT_FOUND, "contact session not found");
        }
        Long currentUserId = AuthContext.currentUserId();
        log.info("农户[{}]确认了联系会话[{}]", currentUserId, id);
        
        if (!currentUserId.equals(contact.getFarmerId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "no permission to confirm contact");
        }
        OrderInfoEntity existingOrder = orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfoEntity>()
                .eq(OrderInfoEntity::getContactSessionId, id)
                .last("limit 1"));
        if (existingOrder != null) {
            DemandEntity existingDemand = demandMapper.selectById(contact.getDemandId());
            if (existingDemand != null && existingDemand.getCurrentOrderId() == null) {
                existingDemand.setCurrentOrderId(existingOrder.getId());
                demandMapper.updateById(existingDemand);
            }
            return Map.of("contactSession", toMap(contact), "orderId", existingOrder.getId(), "demandId", contact.getDemandId());
        }
        // 对于通过浏览直接发起的联系，matchAttemptId 可能为空
        if (contact.getMatchAttemptId() != null) {
            if (!matchFlowService.farmerConfirm(contact.getMatchAttemptId())) {
                throw new BizException(ErrorCode.INVALID_STATE, "current contact session does not allow confirm");
            }
        } else {
            // 直接联系模式：直接激活
            contactSessionMapper.update(null,
                    new LambdaUpdateWrapper<ContactSessionEntity>()
                            .eq(ContactSessionEntity::getId, id)
                            .set(ContactSessionEntity::getStatus, ContactSessionStatus.CONTACT_ACTIVE.name())
                            .set(ContactSessionEntity::getActiveFlag, 1)
                            .set(ContactSessionEntity::getConfirmedAt, LocalDateTime.now()));
        }
        contact = contactSessionMapper.selectById(id);
        OrderInfoEntity order = orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfoEntity>()
                .eq(OrderInfoEntity::getContactSessionId, id)
                .last("limit 1"));
        DemandEntity demand = demandMapper.selectById(contact.getDemandId());
        if (demand != null && order != null) {
            demand.setCurrentOrderId(order.getId());
            demandMapper.updateById(demand);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("contactSession", toMap(contact));
        result.put("orderId", order == null ? null : order.getId());
        result.put("demandId", demand == null ? null : demand.getId());
        return result;
    }

    @Override
    public Map<String, Object> reject(Long id) {
        ContactSessionEntity contact = contactSessionMapper.selectById(id);
        if (contact == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "contact session not found");
        }
        if (!AuthContext.currentUserId().equals(contact.getFarmerId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "no permission to reject contact");
        }
        if (ContactSessionStatus.CONTACT_ACTIVE.name().equals(contact.getStatus())) {
            throw new BizException(ErrorCode.INVALID_STATE, "contact already active");
        }
        if (ContactSessionStatus.CLOSED.name().equals(contact.getStatus()) || ContactSessionStatus.EXPIRED.name().equals(contact.getStatus())) {
            return Map.of("id", id, "rejected", true, "status", contact.getStatus());
        }
        boolean rejected = false;
        if (contact.getMatchAttemptId() != null) {
            rejected = matchFlowService.farmerReject(contact.getMatchAttemptId());
        }
        contactSessionMapper.update(
                null,
                new LambdaUpdateWrapper<ContactSessionEntity>()
                        .eq(ContactSessionEntity::getId, id)
                        .set(ContactSessionEntity::getActiveFlag, 0)
                        .set(ContactSessionEntity::getStatus, ContactSessionStatus.CLOSED.name())
        );
        ContactSessionEntity updated = contactSessionMapper.selectById(id);
        if (contact.getMatchAttemptId() != null) {
            MatchAttemptEntity nextAttempt = matchAttemptMapper.selectOne(new LambdaQueryWrapper<MatchAttemptEntity>()
                    .eq(MatchAttemptEntity::getDemandId, contact.getDemandId())
                    .eq(MatchAttemptEntity::getStatus, "PENDING_RESPONSE")
                    .orderByDesc(MatchAttemptEntity::getId)
                    .last("limit 1"));
            demandMapper.update(
                    null,
                    new LambdaUpdateWrapper<DemandEntity>()
                            .eq(DemandEntity::getId, contact.getDemandId())
                            .set(DemandEntity::getCurrentContactSessionId, null)
                            .set(DemandEntity::getCurrentOrderId, null)
                            .set(DemandEntity::getCurrentMatchAttemptId, nextAttempt == null ? null : nextAttempt.getId())
            );
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("rejected", rejected || true);
        result.put("contactSession", updated == null ? null : toMap(updated));
        result.put("demandId", contact.getDemandId());
        return result;
    }

    // === V10 新增：联系平台核心方法 ===

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> initiateContact(Long ownerId, Long serviceItemId, String contactType, String discoverySource) {
        Long farmerId = AuthContext.currentUserId();
        log.info("农户[{}]对农机手[{}]发起直接联系，服务项ID[{}]", farmerId, ownerId, serviceItemId);

        // 查询农机主信息，获取脱敏电话
        UserAccountEntity owner = userAccountMapper.selectById(ownerId);
        if (owner == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "农机主不存在");
        }

        // 脱敏电话号码（如 138****0002）
        String maskedPhone = maskPhone(owner.getPhone());

        // 创建联系会话（不绑定 matchAttemptId，体现"直接联系"模式）
        ContactSessionEntity session = new ContactSessionEntity();
        session.setFarmerId(farmerId);
        session.setOwnerId(ownerId);
        session.setServiceItemId(serviceItemId);
        session.setStatus(ContactSessionStatus.CONTACTED.name());
        session.setActiveFlag(1);
        session.setMaskedPhone(maskedPhone);
        session.setContactType(contactType != null ? contactType : "PHONE");
        session.setDiscoverySource(discoverySource != null ? discoverySource : "BROWSE");
        session.setContactMode("DIRECT"); // 直接联系模式

        contactSessionMapper.insert(session);
        return toMap(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitFeedback(Long id, String feedback, Integer rating) {
        ContactSessionEntity contact = contactSessionMapper.selectById(id);
        if (contact == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "联系会话不存在");
        }
        Long currentUserId = AuthContext.currentUserId();
        if (!currentUserId.equals(contact.getFarmerId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权提交反馈");
        }
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new BizException(ErrorCode.INVALID_REQUEST, "评分必须在1-5之间");
        }

        contactSessionMapper.update(null,
                new LambdaUpdateWrapper<ContactSessionEntity>()
                        .eq(ContactSessionEntity::getId, id)
                        .set(ContactSessionEntity::getFarmerFeedback, feedback)
                        .set(ContactSessionEntity::getFarmerRating, rating)
                        .set(ContactSessionEntity::getStatus, ContactSessionStatus.FEEDBACK_GIVEN.name()));

        ContactSessionEntity updated = contactSessionMapper.selectById(id);
        return toMap(updated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> markServiceCompleted(Long id) {
        ContactSessionEntity contact = contactSessionMapper.selectById(id);
        if (contact == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "联系会话不存在");
        }

        contactSessionMapper.update(null,
                new LambdaUpdateWrapper<ContactSessionEntity>()
                        .eq(ContactSessionEntity::getId, id)
                        .set(ContactSessionEntity::getServiceCompleted, 1)
                        .set(ContactSessionEntity::getStatus, ContactSessionStatus.SERVING.name()));

        ContactSessionEntity updated = contactSessionMapper.selectById(id);
        return toMap(updated);
    }

    // === 工具方法 ===

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private Map<String, Object> toMap(ContactSessionEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("demandId", entity.getDemandId());
        map.put("matchAttemptId", entity.getMatchAttemptId());
        map.put("ownerId", entity.getOwnerId());
        map.put("farmerId", entity.getFarmerId());
        map.put("serviceItemId", entity.getServiceItemId());
        map.put("status", entity.getStatus());
        map.put("statusLabel", ContactSessionStatus.labelOf(entity.getStatus()));
        map.put("statusDescription", ContactSessionStatus.descriptionOf(entity.getStatus()));
        map.put("activeFlag", entity.getActiveFlag());
        map.put("maskedPhone", entity.getMaskedPhone());
        map.put("contactType", entity.getContactType());
        map.put("discoverySource", entity.getDiscoverySource());
        map.put("farmerFeedback", entity.getFarmerFeedback());
        map.put("farmerRating", entity.getFarmerRating());
        map.put("serviceCompleted", entity.getServiceCompleted());
        map.put("confirmedAt", entity.getConfirmedAt());
        return map;
    }
}
