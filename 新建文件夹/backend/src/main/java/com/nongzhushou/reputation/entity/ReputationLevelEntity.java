package com.nongzhushou.reputation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 老把式信誉等级实体
 * <p>
 * 等级体系：
 * NEWBIE(新手机手) → RELIABLE(靠谱机手) → VETERAN(老把式) → GOLD(金牌机手) → MODEL(村级模范)
 */
@TableName("reputation_level")
public class ReputationLevelEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String levelCode;        // NEWBIE / RELIABLE / VETERAN / GOLD / MODEL
    private String levelName;        // 中文等级名
    private Integer totalServices;   // 累计服务次数
    private BigDecimal goodRate;     // 好评率 0-100
    private Integer hasDisasterRecord; // 是否参与过救灾
    private LocalDateTime upgradedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getLevelCode() { return levelCode; }
    public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
    public Integer getTotalServices() { return totalServices; }
    public void setTotalServices(Integer totalServices) { this.totalServices = totalServices; }
    public BigDecimal getGoodRate() { return goodRate; }
    public void setGoodRate(BigDecimal goodRate) { this.goodRate = goodRate; }
    public Integer getHasDisasterRecord() { return hasDisasterRecord; }
    public void setHasDisasterRecord(Integer hasDisasterRecord) { this.hasDisasterRecord = hasDisasterRecord; }
    public LocalDateTime getUpgradedAt() { return upgradedAt; }
    public void setUpgradedAt(LocalDateTime upgradedAt) { this.upgradedAt = upgradedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
