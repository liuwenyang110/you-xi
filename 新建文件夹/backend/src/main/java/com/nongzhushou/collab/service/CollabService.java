package com.nongzhushou.collab.service;

import java.util.List;
import java.util.Map;

public interface CollabService {

    List<Map<String, Object>> listSessions();

    Map<String, Object> createSession(Long ownerId, Long sourcePostId, Long demandId, String discoverySource, String subject, String initialMessage);

    List<Map<String, Object>> listMessages(Long sessionId);

    Map<String, Object> sendMessage(Long sessionId, String messageType, String content, String mediaUrl);

    Map<String, Object> updateSessionStatus(Long sessionId, String status);

    Map<String, Object> dashboardSummary();
}
