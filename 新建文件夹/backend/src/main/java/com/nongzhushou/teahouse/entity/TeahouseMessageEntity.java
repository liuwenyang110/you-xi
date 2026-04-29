package com.nongzhushou.teahouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("v3_teahouse_message")
public class TeahouseMessageEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teahouseId;
    private Long senderId;
    private String msgType;          // TEXT / IMAGE / VOICE / SYSTEM
    private String content;
    private String mediaKey;         // OSS 对象 Key
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeahouseId() { return teahouseId; }
    public void setTeahouseId(Long teahouseId) { this.teahouseId = teahouseId; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getMsgType() { return msgType; }
    public void setMsgType(String msgType) { this.msgType = msgType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMediaKey() { return mediaKey; }
    public void setMediaKey(String mediaKey) { this.mediaKey = mediaKey; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
