package com.nongzhushou.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("v3_notification")
public class NotificationEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String notiType;         // DEMAND_ASK / DEMAND_WARN / TEAHOUSE_CLOSING / SYSTEM
    private String title;
    private String content;
    private String refType;          // DEMAND / TEAHOUSE
    private Long refId;
    private Integer isRead;
    private Integer needAction;
    private String actionType;       // CONFIRM_COMPLETE / EXTEND_TEAHOUSE
    private Integer actionDone;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNotiType() { return notiType; }
    public void setNotiType(String notiType) { this.notiType = notiType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }
    public Long getRefId() { return refId; }
    public void setRefId(Long refId) { this.refId = refId; }
    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }
    public Integer getNeedAction() { return needAction; }
    public void setNeedAction(Integer needAction) { this.needAction = needAction; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public Integer getActionDone() { return actionDone; }
    public void setActionDone(Integer actionDone) { this.actionDone = actionDone; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
