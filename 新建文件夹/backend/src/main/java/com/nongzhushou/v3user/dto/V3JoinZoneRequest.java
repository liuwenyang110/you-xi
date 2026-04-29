package com.nongzhushou.v3user.dto;

import jakarta.validation.constraints.NotNull;

public class V3JoinZoneRequest {

    @NotNull(message = "片区ID不能为空")
    private Long zoneId;

    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
}
