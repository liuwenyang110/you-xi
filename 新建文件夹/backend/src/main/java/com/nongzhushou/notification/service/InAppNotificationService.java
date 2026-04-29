package com.nongzhushou.notification.service;

import com.nongzhushou.notification.entity.NotificationEntity;
import com.nongzhushou.notification.mapper.NotificationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * APP 内弹窗通知实现（默认渠道）
 * 消息写入 v3_notification 表，前端通过轮询或 WebSocket 拉取
 */
@Service
public class InAppNotificationService implements NotificationStrategy {

    private static final Logger log = LoggerFactory.getLogger(InAppNotificationService.class);

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public void send(Long userId, String notiType, String title, String content,
                     String refType, Long refId, boolean needAction, String actionType) {
        NotificationEntity noti = new NotificationEntity();
        noti.setUserId(userId);
        noti.setNotiType(notiType);
        noti.setTitle(title);
        noti.setContent(content);
        noti.setRefType(refType);
        noti.setRefId(refId);
        noti.setIsRead(0);
        noti.setNeedAction(needAction ? 1 : 0);
        noti.setActionType(actionType);
        noti.setActionDone(0);
        noti.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(noti);
        log.info("APP通知已发送: userId={}, type={}, title={}", userId, notiType, title);
    }

    /** 获取用户通知列表（分页） */
    public Map<String, Object> getUserNotifications(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<NotificationEntity> list = notificationMapper.findByUser(userId, size, offset);
        int unreadCount = notificationMapper.countUnreadByUser(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("unreadCount", unreadCount);
        result.put("page", page);
        return result;
    }

    /** 一键全部已读 */
    public void markAllRead(Long userId) {
        notificationMapper.markAllReadByUser(userId);
    }

    /** 标记单条已读 */
    public void markRead(Long notiId) {
        NotificationEntity noti = notificationMapper.selectById(notiId);
        if (noti != null) {
            noti.setIsRead(1);
            notificationMapper.updateById(noti);
        }
    }

    /** 标记操作已完成 */
    public void markActionDone(Long notiId) {
        NotificationEntity noti = notificationMapper.selectById(notiId);
        if (noti != null) {
            noti.setActionDone(1);
            noti.setIsRead(1);
            notificationMapper.updateById(noti);
        }
    }
}
