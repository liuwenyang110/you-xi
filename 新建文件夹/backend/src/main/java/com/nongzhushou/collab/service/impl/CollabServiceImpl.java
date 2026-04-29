package com.nongzhushou.collab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nongzhushou.collab.entity.CollabMessageEntity;
import com.nongzhushou.collab.entity.CollabSessionEventEntity;
import com.nongzhushou.collab.mapper.CollabMessageMapper;
import com.nongzhushou.collab.mapper.CollabSessionEventMapper;
import com.nongzhushou.collab.service.CollabService;
import com.nongzhushou.common.exception.BizException;
import com.nongzhushou.common.exception.ErrorCode;
import com.nongzhushou.common.security.AuthContext;
import com.nongzhushou.community.entity.CommunityPostEntity;
import com.nongzhushou.community.mapper.CommunityPostMapper;
import com.nongzhushou.contact.entity.ContactSessionEntity;
import com.nongzhushou.contact.mapper.ContactSessionMapper;
import com.nongzhushou.demand.entity.DemandEntity;
import com.nongzhushou.demand.mapper.DemandMapper;
import com.nongzhushou.user.entity.UserAccountEntity;
import com.nongzhushou.user.mapper.UserAccountMapper;
import com.nongzhushou.volunteer.entity.VolunteerClaimEntity;
import com.nongzhushou.volunteer.mapper.VolunteerClaimMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollabServiceImpl implements CollabService {

    private final ContactSessionMapper contactSessionMapper;
    private final CollabMessageMapper collabMessageMapper;
    private final CollabSessionEventMapper collabSessionEventMapper;
    private final CommunityPostMapper communityPostMapper;
    private final VolunteerClaimMapper volunteerClaimMapper;
    private final DemandMapper demandMapper;
    private final UserAccountMapper userAccountMapper;

    public CollabServiceImpl(
            ContactSessionMapper contactSessionMapper,
            CollabMessageMapper collabMessageMapper,
            CollabSessionEventMapper collabSessionEventMapper,
            CommunityPostMapper communityPostMapper,
            VolunteerClaimMapper volunteerClaimMapper,
            DemandMapper demandMapper,
            UserAccountMapper userAccountMapper
    ) {
        this.contactSessionMapper = contactSessionMapper;
        this.collabMessageMapper = collabMessageMapper;
        this.collabSessionEventMapper = collabSessionEventMapper;
        this.communityPostMapper = communityPostMapper;
        this.volunteerClaimMapper = volunteerClaimMapper;
        this.demandMapper = demandMapper;
        this.userAccountMapper = userAccountMapper;
    }

    @Override
    public List<Map<String, Object>> listSessions() {
        Long currentUserId = AuthContext.currentUserId();
        return contactSessionMapper.selectList(new LambdaQueryWrapper<ContactSessionEntity>()
                        .and(wrapper -> wrapper.eq(ContactSessionEntity::getFarmerId, currentUserId)
                                .or()
                                .eq(ContactSessionEntity::getOwnerId, currentUserId))
                        .orderByDesc(ContactSessionEntity::getLastMessageAt)
                        .orderByDesc(ContactSessionEntity::getId))
                .stream()
                .map(session -> toSessionMap(session, currentUserId))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createSession(Long ownerId, Long sourcePostId, Long demandId, String discoverySource, String subject, String initialMessage) {
        Long currentUserId = AuthContext.currentUserId();
        ownerId = resolveOwnerId(ownerId, sourcePostId);
        if (currentUserId.equals(ownerId)) {
            throw new BizException(ErrorCode.INVALID_REQUEST, "不能与自己发起公益协作会话");
        }
        UserAccountEntity owner = userAccountMapper.selectById(ownerId);
        if (owner == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "服务者不存在");
        }

        ContactSessionEntity existing = contactSessionMapper.selectOne(new LambdaQueryWrapper<ContactSessionEntity>()
                .eq(ContactSessionEntity::getFarmerId, currentUserId)
                .eq(ContactSessionEntity::getOwnerId, ownerId)
                .eq(ContactSessionEntity::getSessionType, "PUBLIC_GOOD")
                .eq(sourcePostId != null, ContactSessionEntity::getSourcePostId, sourcePostId)
                .eq(demandId != null, ContactSessionEntity::getDemandId, demandId)
                .last("LIMIT 1"));
        if (existing != null) {
            if (initialMessage != null && !initialMessage.isBlank()) {
                appendMessage(existing, currentUserId, "TEXT", initialMessage, null);
            }
            ContactSessionEntity latestSession = contactSessionMapper.selectById(existing.getId());
            return toSessionMap(latestSession == null ? existing : latestSession, currentUserId);
        }

        ContactSessionEntity session = new ContactSessionEntity();
        session.setFarmerId(currentUserId);
        session.setOwnerId(ownerId);
        session.setDemandId(demandId);
        session.setSourcePostId(sourcePostId);
        session.setSessionType("PUBLIC_GOOD");
        session.setDiscoverySource(defaultString(discoverySource, "POST"));
        session.setSubject(buildSubject(subject, sourcePostId, demandId));
        session.setSummary(buildSummary(sourcePostId, demandId));
        session.setStatus("CONTACTED");
        session.setActiveFlag(1);
        session.setContactType("WECHAT");
        session.setDeliveryMode("POLLING");
        session.setUnreadFarmerCount(0);
        session.setUnreadOwnerCount(0);
        session.setLastAckAt(LocalDateTime.now());
        session.setLastMessageAt(LocalDateTime.now());
        session.setLastMessagePreview("已建立公益协作会话");
        session.setLastSenderId(currentUserId);
        session.setMaskedPhone(maskPhone(owner.getPhone()));
        contactSessionMapper.insert(session);

        insertSystemMessage(session.getId(), "SYSTEM", "平台仅协助公益协作沟通，不参与定价与交易。");
        insertEvent(session.getId(), "SESSION_CREATED", "创建公益协作会话", currentUserId);

        if (initialMessage != null && !initialMessage.isBlank()) {
            appendMessage(session, currentUserId, "TEXT", initialMessage, null);
        }

        return toSessionMap(session, currentUserId);
    }

    @Override
    public List<Map<String, Object>> listMessages(Long sessionId) {
        Long currentUserId = AuthContext.currentUserId();
        ContactSessionEntity session = requireParticipantSession(sessionId, currentUserId);
        markSessionRead(session, currentUserId);
        return collabMessageMapper.selectList(new LambdaQueryWrapper<CollabMessageEntity>()
                        .eq(CollabMessageEntity::getSessionId, sessionId)
                        .orderByAsc(CollabMessageEntity::getId))
                .stream()
                .map(message -> toMessageMap(message, currentUserId, session))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> sendMessage(Long sessionId, String messageType, String content, String mediaUrl) {
        Long currentUserId = AuthContext.currentUserId();
        ContactSessionEntity session = requireParticipantSession(sessionId, currentUserId);
        if (isClosedStatus(session.getStatus())) {
            throw new BizException(ErrorCode.INVALID_STATE, "会话已关闭，不能继续发送消息");
        }
        if ((content == null || content.isBlank()) && (mediaUrl == null || mediaUrl.isBlank())) {
            throw new BizException(ErrorCode.INVALID_REQUEST, "消息内容不能为空");
        }

        return appendMessage(session, currentUserId, messageType, content, mediaUrl);
    }

    private Map<String, Object> appendMessage(ContactSessionEntity session, Long currentUserId, String messageType, String content, String mediaUrl) {
        CollabMessageEntity entity = new CollabMessageEntity();
        entity.setSessionId(session.getId());
        entity.setSenderId(currentUserId);
        entity.setSenderRole(currentUserId.equals(session.getFarmerId()) ? "FARMER" : "OWNER");
        entity.setMessageType(defaultString(messageType, "TEXT"));
        entity.setContent(trimToNull(content));
        entity.setMediaUrl(trimToNull(mediaUrl));
        entity.setSystemFlag(0);
        entity.setCreatedAt(LocalDateTime.now());
        collabMessageMapper.insert(entity);

        if (currentUserId.equals(session.getFarmerId())) {
            session.setUnreadOwnerCount(defaultInt(session.getUnreadOwnerCount()) + 1);
        } else {
            session.setUnreadFarmerCount(defaultInt(session.getUnreadFarmerCount()) + 1);
        }
        session.setLastSenderId(currentUserId);
        session.setLastMessageAt(entity.getCreatedAt());
        session.setLastMessagePreview(previewOf(entity));
        contactSessionMapper.updateById(session);

        return toMessageMap(entity, currentUserId, session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateSessionStatus(Long sessionId, String status) {
        Long currentUserId = AuthContext.currentUserId();
        ContactSessionEntity session = requireParticipantSession(sessionId, currentUserId);
        validateStatusTransition(session.getStatus(), status);
        session.setStatus(status);
        session.setUpdatedAt(LocalDateTime.now());
        contactSessionMapper.updateById(session);

        insertEvent(sessionId, "STATUS_CHANGED", "状态更新为 " + status, currentUserId);
        insertSystemMessage(sessionId, "SYSTEM", "协作状态已更新为 " + status);

        return toSessionMap(contactSessionMapper.selectById(sessionId), currentUserId);
    }

    @Override
    public Map<String, Object> dashboardSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        long sessionCount = contactSessionMapper.selectCount(new LambdaQueryWrapper<ContactSessionEntity>()
                .eq(ContactSessionEntity::getSessionType, "PUBLIC_GOOD"));
        long messageCount = collabMessageMapper.selectCount(null);
        long eventCount = collabSessionEventMapper.selectCount(null);
        long activeClaims = volunteerClaimMapper.selectCount(new LambdaQueryWrapper<VolunteerClaimEntity>()
                .in(VolunteerClaimEntity::getStatus, List.of("CLAIMED", "EN_ROUTE", "WORKING")));
        long activePosts = communityPostMapper.selectCount(new LambdaQueryWrapper<CommunityPostEntity>()
                .eq(CommunityPostEntity::getStatus, "ACTIVE"));
        long resolvedDemands = demandMapper.selectCount(new LambdaQueryWrapper<DemandEntity>()
                .in(DemandEntity::getStatus, List.of("COMPLETED", "CLOSED")));
        summary.put("collabSessionCount", sessionCount);
        summary.put("collabMessageCount", messageCount);
        summary.put("collabEventCount", eventCount);
        summary.put("activeVolunteerClaims", activeClaims);
        summary.put("activeCommunityPosts", activePosts);
        summary.put("resolvedDemandCount", resolvedDemands);
        summary.put("responseRate", sessionCount == 0 ? 0D : Math.round((resolvedDemands * 1000D / sessionCount)) / 10D);
        return summary;
    }

    private ContactSessionEntity requireParticipantSession(Long sessionId, Long currentUserId) {
        ContactSessionEntity session = contactSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "会话不存在");
        }
        if (!currentUserId.equals(session.getFarmerId()) && !currentUserId.equals(session.getOwnerId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权访问该协作会话");
        }
        return session;
    }

    private void markSessionRead(ContactSessionEntity session, Long currentUserId) {
        boolean changed = false;
        if (currentUserId.equals(session.getFarmerId()) && defaultInt(session.getUnreadFarmerCount()) > 0) {
            session.setUnreadFarmerCount(0);
            changed = true;
        }
        if (currentUserId.equals(session.getOwnerId()) && defaultInt(session.getUnreadOwnerCount()) > 0) {
            session.setUnreadOwnerCount(0);
            changed = true;
        }
        if (changed) {
            session.setLastAckAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
            contactSessionMapper.updateById(session);
        }
    }

    private Long resolveOwnerId(Long ownerId, Long sourcePostId) {
        if (ownerId != null) {
            return ownerId;
        }
        if (sourcePostId != null) {
            CommunityPostEntity post = communityPostMapper.selectById(sourcePostId);
            if (post != null && post.getAuthorId() != null) {
                if (!"ACTIVE".equalsIgnoreCase(defaultString(post.getStatus(), "ACTIVE"))) {
                    throw new BizException(ErrorCode.INVALID_STATE, "公益内容已关闭，无法继续发起协作");
                }
                return post.getAuthorId();
            }
        }
        throw new BizException(ErrorCode.INVALID_REQUEST, "缺少协作对象");
    }

    private void validateStatusTransition(String currentStatus, String nextStatus) {
        String normalizedCurrent = defaultString(currentStatus, "CONTACTED").toUpperCase();
        String normalizedNext = defaultString(nextStatus, "").toUpperCase();
        Map<String, List<String>> transitions = Map.of(
                "CONTACTED", List.of("CLAIMED", "IN_PROGRESS", "COMPLETED", "CLOSED"),
                "CLAIMED", List.of("IN_PROGRESS", "COMPLETED", "CLOSED"),
                "IN_PROGRESS", List.of("COMPLETED", "CLOSED"),
                "COMPLETED", List.of(),
                "CLOSED", List.of()
        );
        List<String> allowed = transitions.getOrDefault(normalizedCurrent, List.of());
        if (!allowed.contains(normalizedNext) && !normalizedCurrent.equals(normalizedNext)) {
            throw new BizException(ErrorCode.INVALID_STATE, "当前状态不允许切换到目标状态");
        }
    }

    private void insertSystemMessage(Long sessionId, String messageType, String content) {
        CollabMessageEntity systemMessage = new CollabMessageEntity();
        systemMessage.setSessionId(sessionId);
        systemMessage.setSenderId(0L);
        systemMessage.setSenderRole("SYSTEM");
        systemMessage.setMessageType(messageType);
        systemMessage.setContent(content);
        systemMessage.setSystemFlag(1);
        systemMessage.setCreatedAt(LocalDateTime.now());
        collabMessageMapper.insert(systemMessage);
    }

    private void insertEvent(Long sessionId, String eventType, String detail, Long operatorId) {
        CollabSessionEventEntity event = new CollabSessionEventEntity();
        event.setSessionId(sessionId);
        event.setEventType(eventType);
        event.setEventDetail(detail);
        event.setOperatorId(operatorId);
        event.setCreatedAt(LocalDateTime.now());
        collabSessionEventMapper.insert(event);
    }

    private Map<String, Object> toSessionMap(ContactSessionEntity session, Long currentUserId) {
        UserAccountEntity counterpart = userAccountMapper.selectById(currentUserId.equals(session.getFarmerId()) ? session.getOwnerId() : session.getFarmerId());
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", session.getId());
        map.put("demandId", session.getDemandId());
        map.put("sourcePostId", session.getSourcePostId());
        map.put("status", session.getStatus());
        map.put("subject", defaultString(session.getSubject(), "公益协作会话"));
        map.put("summary", defaultString(session.getSummary(), ""));
        map.put("sessionType", defaultString(session.getSessionType(), "PUBLIC_GOOD"));
        map.put("deliveryMode", defaultString(session.getDeliveryMode(), "POLLING"));
        map.put("lastMessagePreview", defaultString(session.getLastMessagePreview(), "等待新消息"));
        map.put("lastMessageAt", session.getLastMessageAt());
        map.put("unreadCount", currentUserId.equals(session.getFarmerId()) ? defaultInt(session.getUnreadFarmerCount()) : defaultInt(session.getUnreadOwnerCount()));
        map.put("counterpartId", counterpart == null ? null : counterpart.getId());
        map.put("counterpartName", counterpart == null ? "协作伙伴" : defaultString(counterpart.getNickname(), fallbackName(counterpart)));
        map.put("counterpartAvatar", counterpart == null ? null : counterpart.getAvatarUrl());
        map.put("publicNotice", "仅用于公益协作沟通，平台不参与交易和定价");
        return map;
    }

    private Map<String, Object> toMessageMap(CollabMessageEntity message, Long currentUserId, ContactSessionEntity session) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", message.getId());
        map.put("sessionId", message.getSessionId());
        map.put("senderId", message.getSenderId());
        map.put("senderRole", message.getSenderRole());
        map.put("messageType", message.getMessageType());
        map.put("content", message.getContent());
        map.put("mediaUrl", message.getMediaUrl());
        map.put("systemFlag", message.getSystemFlag());
        map.put("createdAt", message.getCreatedAt());
        map.put("fromCounterpart", !message.getSenderId().equals(currentUserId) && message.getSenderId() != 0L);
        map.put("sessionStatus", session.getStatus());
        return map;
    }

    private String buildSubject(String subject, Long sourcePostId, Long demandId) {
        if (subject != null && !subject.isBlank()) {
            return subject.trim();
        }
        if (sourcePostId != null) {
            CommunityPostEntity post = communityPostMapper.selectById(sourcePostId);
            if (post != null && post.getTitle() != null && !post.getTitle().isBlank()) {
                return post.getTitle();
            }
        }
        if (demandId != null) {
            DemandEntity demand = demandMapper.selectById(demandId);
            if (demand != null && demand.getRemark() != null && !demand.getRemark().isBlank()) {
                return demand.getRemark();
            }
        }
        return "公益协作会话";
    }

    private String buildSummary(Long sourcePostId, Long demandId) {
        if (sourcePostId != null) {
            CommunityPostEntity post = communityPostMapper.selectById(sourcePostId);
            if (post != null && post.getContent() != null) {
                return post.getContent();
            }
        }
        if (demandId != null) {
            DemandEntity demand = demandMapper.selectById(demandId);
            if (demand != null) {
                return defaultString(demand.getVillageName(), "") + " " + defaultString(demand.getRemark(), "");
            }
        }
        return "";
    }

    private String previewOf(CollabMessageEntity entity) {
        if ("IMAGE".equalsIgnoreCase(entity.getMessageType())) {
            return "[图片]";
        }
        if ("QUICK_REPLY".equalsIgnoreCase(entity.getMessageType())) {
            return "[快捷回复] " + defaultString(entity.getContent(), "");
        }
        return defaultString(entity.getContent(), "[系统消息]");
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String fallbackName(UserAccountEntity user) {
        if (user.getPhone() == null || user.getPhone().length() < 4) {
            return "协作伙伴";
        }
        return "用户" + user.getPhone().substring(user.getPhone().length() - 4);
    }

    private boolean isClosedStatus(String status) {
        return "CLOSED".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status);
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
