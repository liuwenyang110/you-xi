package com.nongzhushou.notification.service;

/**
 * 通知渠道策略接口（Strategy 模式）
 * 当前实现：APP 内弹窗
 * 未来可扩展：微信模板消息 / 短信
 */
public interface NotificationStrategy {

    /**
     * 发送通知
     * @param userId    接收用户 v3_user.id
     * @param notiType  通知类型：DEMAND_ASK / DEMAND_WARN / TEAHOUSE_CLOSING / SYSTEM
     * @param title     标题
     * @param content   正文
     * @param refType   关联业务类型：DEMAND / TEAHOUSE
     * @param refId     关联业务ID
     * @param needAction 是否需要用户操作
     * @param actionType 操作类型：CONFIRM_COMPLETE / EXTEND_TEAHOUSE
     */
    void send(Long userId, String notiType, String title, String content,
              String refType, Long refId, boolean needAction, String actionType);
}
