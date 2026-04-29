package com.nongzhushou.collab.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nongzhushou.collab.entity.CollabMessageEntity;
import com.nongzhushou.collab.entity.CollabSessionEventEntity;
import com.nongzhushou.collab.mapper.CollabMessageMapper;
import com.nongzhushou.collab.mapper.CollabSessionEventMapper;
import com.nongzhushou.common.exception.BizException;
import com.nongzhushou.common.security.AuthContext;
import com.nongzhushou.community.entity.CommunityPostEntity;
import com.nongzhushou.community.mapper.CommunityPostMapper;
import com.nongzhushou.contact.entity.ContactSessionEntity;
import com.nongzhushou.contact.mapper.ContactSessionMapper;
import com.nongzhushou.demand.mapper.DemandMapper;
import com.nongzhushou.user.entity.UserAccountEntity;
import com.nongzhushou.user.mapper.UserAccountMapper;
import com.nongzhushou.volunteer.mapper.VolunteerClaimMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CollabServiceImplTest {

    @Mock private ContactSessionMapper contactSessionMapper;
    @Mock private CollabMessageMapper collabMessageMapper;
    @Mock private CollabSessionEventMapper collabSessionEventMapper;
    @Mock private CommunityPostMapper communityPostMapper;
    @Mock private VolunteerClaimMapper volunteerClaimMapper;
    @Mock private DemandMapper demandMapper;
    @Mock private UserAccountMapper userAccountMapper;

    private CollabServiceImpl collabService;

    @BeforeEach
    void setUp() {
        collabService = new CollabServiceImpl(
                contactSessionMapper,
                collabMessageMapper,
                collabSessionEventMapper,
                communityPostMapper,
                volunteerClaimMapper,
                demandMapper,
                userAccountMapper
        );
    }

    @Test
    void createSession_createsSessionAndSystemMessageForFarmer() {
        UserAccountEntity owner = new UserAccountEntity();
        owner.setId(2002L);
        owner.setNickname("李师傅");
        owner.setAvatarUrl("https://example.com/owner.png");
        owner.setPhone("13800001111");
        when(userAccountMapper.selectById(2002L)).thenReturn(owner);

        CommunityPostEntity post = new CommunityPostEntity();
        post.setId(9L);
        post.setTitle("紧急收割求助");
        post.setContent("今晚变天前想完成收割");
        when(communityPostMapper.selectById(9L)).thenReturn(post);

        try (MockedStatic<AuthContext> authContext = org.mockito.Mockito.mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::currentUserId).thenReturn(1001L);

            Map<String, Object> result = collabService.createSession(2002L, 9L, 12L, "POST", "求助协作", "农户发起联系");

            assertEquals("CONTACTED", result.get("status"));
            assertEquals("求助协作", result.get("subject"));
            verify(contactSessionMapper).insert(any(ContactSessionEntity.class));
            verify(collabMessageMapper, times(2)).insert(any(CollabMessageEntity.class));
            verify(collabSessionEventMapper).insert(any(CollabSessionEventEntity.class));
        }
    }

    @Test
    void sendMessage_rejectsBlankContent() {
        ContactSessionEntity session = buildSession();
        when(contactSessionMapper.selectById(1L)).thenReturn(session);

        try (MockedStatic<AuthContext> authContext = org.mockito.Mockito.mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::currentUserId).thenReturn(1001L);

            assertThrows(BizException.class, () -> collabService.sendMessage(1L, "TEXT", "   ", null));
            verify(collabMessageMapper, never()).insert(any(CollabMessageEntity.class));
        }
    }

    @Test
    void updateSessionStatus_updatesSessionAndCreatesSystemEvent() {
        ContactSessionEntity session = buildSession();
        session.setStatus("CONTACTED");
        when(contactSessionMapper.selectById(1L)).thenReturn(session);

        try (MockedStatic<AuthContext> authContext = org.mockito.Mockito.mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::currentUserId).thenReturn(2002L);

            Map<String, Object> result = collabService.updateSessionStatus(1L, "IN_PROGRESS");

            assertEquals("IN_PROGRESS", result.get("status"));
            verify(contactSessionMapper).updateById(any(ContactSessionEntity.class));
            verify(collabMessageMapper).insert(any(CollabMessageEntity.class));
            verify(collabSessionEventMapper).insert(any(CollabSessionEventEntity.class));
        }
    }

    @Test
    void listMessages_marksUnreadFromCounterpart() {
        ContactSessionEntity session = buildSession();
        session.setUnreadFarmerCount(3);
        when(contactSessionMapper.selectById(1L)).thenReturn(session);

        CollabMessageEntity mine = new CollabMessageEntity();
        mine.setId(11L);
        mine.setSessionId(1L);
        mine.setSenderId(1001L);
        mine.setMessageType("TEXT");
        mine.setContent("我这边可以配合");
        mine.setCreatedAt(LocalDateTime.now().minusMinutes(3));

        CollabMessageEntity other = new CollabMessageEntity();
        other.setId(12L);
        other.setSessionId(1L);
        other.setSenderId(2002L);
        other.setMessageType("TEXT");
        other.setContent("我半小时内到");
        other.setCreatedAt(LocalDateTime.now().minusMinutes(1));

        when(collabMessageMapper.selectList(any())).thenReturn(List.of(mine, other));

        try (MockedStatic<AuthContext> authContext = org.mockito.Mockito.mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::currentUserId).thenReturn(1001L);

            List<Map<String, Object>> result = collabService.listMessages(1L);

            assertEquals(2, result.size());
            assertEquals(Boolean.FALSE, result.get(0).get("fromCounterpart"));
            assertEquals(Boolean.TRUE, result.get(1).get("fromCounterpart"));
            assertEquals(0, session.getUnreadFarmerCount());
            verify(contactSessionMapper).updateById(session);
        }
    }

    @Test
    void createSession_derivesOwnerFromPostAndReusesExistingSession() {
        CommunityPostEntity post = new CommunityPostEntity();
        post.setId(9L);
        post.setAuthorId(2002L);
        post.setTitle("紧急收割求助");
        post.setContent("今夜下雨前需要协作");
        when(communityPostMapper.selectById(9L)).thenReturn(post);

        UserAccountEntity owner = new UserAccountEntity();
        owner.setId(2002L);
        owner.setNickname("李师傅");
        owner.setPhone("13800001111");
        when(userAccountMapper.selectById(2002L)).thenReturn(owner);

        ContactSessionEntity existing = buildSession();
        existing.setSourcePostId(9L);
        existing.setSubject("已存在协作");
        when(contactSessionMapper.selectOne(any())).thenReturn(existing);

        try (MockedStatic<AuthContext> authContext = org.mockito.Mockito.mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::currentUserId).thenReturn(1001L);

            Map<String, Object> result = collabService.createSession(null, 9L, null, "POST", null, "继续沟通");

            assertEquals(1L, result.get("id"));
            assertEquals("已存在协作", result.get("subject"));
            verify(contactSessionMapper, never()).insert(any(ContactSessionEntity.class));
            verify(collabMessageMapper).insert(any(CollabMessageEntity.class));
        }
    }

    @Test
    void createSession_rejectsSelfSession() {
        CommunityPostEntity post = new CommunityPostEntity();
        post.setId(9L);
        post.setAuthorId(1001L);
        when(communityPostMapper.selectById(9L)).thenReturn(post);

        try (MockedStatic<AuthContext> authContext = org.mockito.Mockito.mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::currentUserId).thenReturn(1001L);

            Assertions.assertThrows(BizException.class, () -> collabService.createSession(null, 9L, null, "POST", null, null));
            verify(contactSessionMapper, never()).insert(any(ContactSessionEntity.class));
        }
    }

    @Test
    void createSession_rejectsClosedPost() {
        CommunityPostEntity post = new CommunityPostEntity();
        post.setId(9L);
        post.setAuthorId(2002L);
        post.setStatus("CLOSED");
        when(communityPostMapper.selectById(9L)).thenReturn(post);

        try (MockedStatic<AuthContext> authContext = org.mockito.Mockito.mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::currentUserId).thenReturn(1001L);

            assertThrows(BizException.class, () -> collabService.createSession(null, 9L, null, "POST", null, null));
            verify(userAccountMapper, never()).selectById(2002L);
            verify(contactSessionMapper, never()).insert(any(ContactSessionEntity.class));
        }
    }

    @Test
    void updateSessionStatus_rejectsInvalidBackwardTransition() {
        ContactSessionEntity session = buildSession();
        session.setStatus("COMPLETED");
        when(contactSessionMapper.selectById(1L)).thenReturn(session);

        try (MockedStatic<AuthContext> authContext = org.mockito.Mockito.mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::currentUserId).thenReturn(2002L);

            assertThrows(BizException.class, () -> collabService.updateSessionStatus(1L, "IN_PROGRESS"));
            verify(contactSessionMapper, never()).updateById(any(ContactSessionEntity.class));
        }
    }

    private ContactSessionEntity buildSession() {
        ContactSessionEntity session = new ContactSessionEntity();
        session.setId(1L);
        session.setFarmerId(1001L);
        session.setOwnerId(2002L);
        session.setStatus("CONTACTED");
        session.setContactType("WECHAT");
        session.setCreatedAt(LocalDateTime.now().minusDays(1));
        return session;
    }
}
