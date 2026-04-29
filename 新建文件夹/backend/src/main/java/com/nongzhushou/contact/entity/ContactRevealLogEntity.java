package com.nongzhushou.contact.entity;

import java.time.LocalDateTime;

/**
 * 公益联系行为日志 Entity
 * 对应数据库表 contact_reveal_log
 */
public class ContactRevealLogEntity {

    private Long id;

    /** 发起联系的农户 ID */
    private Long farmerId;

    /** 被联系的服务者 ID */
    private Long operatorId;

    /** 所在片区 ID（可为 null） */
    private Long zoneId;

    /** 联系来源: OPERATOR_DETAIL / ZONE_HOME / DEMAND_LIST / HOME */
    private String source;

    /** 记录时间 */
    private LocalDateTime createdAt;

    // ───── Getters / Setters ─────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }

    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
