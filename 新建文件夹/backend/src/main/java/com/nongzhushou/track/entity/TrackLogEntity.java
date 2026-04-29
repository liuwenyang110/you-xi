package com.nongzhushou.track.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("nong_track_log")
public class TrackLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long ownerId;
    private BigDecimal lat;
    private BigDecimal lng;
    private Float speed;
    private Float accuracy;
    private Long deviceTime;
    private Integer isCorrected;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }
    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }
    public Float getSpeed() { return speed; }
    public void setSpeed(Float speed) { this.speed = speed; }
    public Float getAccuracy() { return accuracy; }
    public void setAccuracy(Float accuracy) { this.accuracy = accuracy; }
    public Long getDeviceTime() { return deviceTime; }
    public void setDeviceTime(Long deviceTime) { this.deviceTime = deviceTime; }
    public Integer getIsCorrected() { return isCorrected; }
    public void setIsCorrected(Integer isCorrected) { this.isCorrected = isCorrected; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
