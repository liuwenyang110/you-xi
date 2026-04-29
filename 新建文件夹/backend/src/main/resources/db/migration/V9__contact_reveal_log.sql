-- =============================================
-- V9: 公益联系行为记录日志
-- 平台使命：免费提供信息对接，不参与任何商业交易
-- 本表用于记录"农户查看了服务者联系方式"的行为，
-- 作为平台公益价值统计的核心数据源
-- =============================================

-- 1. 联系行为记录日志（公益对接日志表）
CREATE TABLE IF NOT EXISTS contact_reveal_log (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    farmer_id       BIGINT      NOT NULL COMMENT '发起联系的农户 user_account.id',
    operator_id     BIGINT      NOT NULL COMMENT '被联系的服务者 user_account.id',
    zone_id         BIGINT      NULL     COMMENT '所在片区（冗余，方便按区统计）',
    source          VARCHAR(30) NOT NULL DEFAULT 'OPERATOR_DETAIL'
                                         COMMENT '联系来源: OPERATOR_DETAIL / ZONE_HOME / DEMAND_LIST / HOME',
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '联系时间',

    INDEX idx_farmer   (farmer_id),
    INDEX idx_operator (operator_id),
    INDEX idx_zone     (zone_id),
    INDEX idx_created  (created_at DESC),
    -- 复合索引：按日期聚合统计用
    INDEX idx_date_zone (zone_id, created_at)
) COMMENT='公益联系行为日志 — 平台核心公益指标数据源';

-- 2. 公益平台运营快照表（每日定时聚合，供 Admin 看板高效读取）
CREATE TABLE IF NOT EXISTS platform_daily_stats (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_date        DATE        NOT NULL COMMENT '统计日期',
    zone_id          BIGINT      NULL     COMMENT 'NULL=全平台合计',
    total_demands    INT         NOT NULL DEFAULT 0 COMMENT '当日新增需求数',
    total_connected  INT         NOT NULL DEFAULT 0 COMMENT '当日联系成功次数（contact_reveal_log 记录数）',
    total_operators  INT         NOT NULL DEFAULT 0 COMMENT '截至当日注册服务者总数',
    covered_zones    INT         NOT NULL DEFAULT 0 COMMENT '有活动记录的片区数',
    created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE INDEX uidx_date_zone (stat_date, zone_id)
) COMMENT='每日运营数据快照 — 公益看板缓存表，避免实时聚合压力';
