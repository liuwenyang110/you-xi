-- =============================================
-- V13: 村级快闪茶馆 + 消息表
-- 支撑 7天预警 / 延期 / 15天强杀 / 阅后即焚 机制
-- =============================================

-- -----------------------------------------------
-- 1. 村级茶馆表（v3_zone_teahouse）
--    每个村一个茶馆实例，记录完整生命周期
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS v3_zone_teahouse (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    zone_id         BIGINT       NOT NULL COMMENT '所属片区（村）v3_zone.id',
    status          VARCHAR(20)  NOT NULL DEFAULT 'OPEN'
                    COMMENT 'OPEN开张/CLOSING_WARN预警中/EXTENDED已延期/CLOSED已关闭',
    -- 需求活跃度追踪
    last_demand_at  DATETIME     NULL COMMENT '该区域最后一条需求的发布时间',
    -- 7天预警
    warn_sent_at    DATETIME     NULL COMMENT '关闭预警公告的发送时间',
    -- 延期
    extend_days     INT          NULL COMMENT '农户申请的延期天数',
    extended_by     BIGINT       NULL COMMENT '申请延期的农户 v3_user.id',
    extended_at     DATETIME     NULL COMMENT '延期申请时间',
    -- 强制关闭
    force_closed_at DATETIME     NULL COMMENT '15天硬底线触发的强制关闭时间',
    -- 基础
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '茶馆开张时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_teahouse_zone (zone_id),
    INDEX idx_teahouse_status (status),
    INDEX idx_teahouse_last_demand (last_demand_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='村级快闪茶馆';


-- -----------------------------------------------
-- 2. 茶馆消息表（v3_teahouse_message）
--    支持文字/图片/语音/系统公告
--    茶馆关闭时物理删除全部消息
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS v3_teahouse_message (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    teahouse_id     BIGINT       NOT NULL COMMENT '所属茶馆',
    sender_id       BIGINT       NULL COMMENT '发言者 v3_user.id，系统消息为NULL',
    msg_type        VARCHAR(20)  NOT NULL DEFAULT 'TEXT'
                    COMMENT 'TEXT文字/IMAGE图片/VOICE语音/SYSTEM系统公告',
    content         TEXT         NULL COMMENT '文字内容或媒体URL',
    media_key       VARCHAR(200) NULL COMMENT 'OSS对象Key（图片/语音），清理时用',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_msg_teahouse (teahouse_id, created_at),
    INDEX idx_msg_sender (sender_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='茶馆消息表';


-- -----------------------------------------------
-- 3. APP内通知表（v3_notification）
--    可插拔通知渠道的基础：当前APP弹窗，未来微信/短信
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS v3_notification (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '接收者 v3_user.id',
    noti_type       VARCHAR(30)  NOT NULL COMMENT '通知类型：DEMAND_ASK/DEMAND_WARN/TEAHOUSE_CLOSING/SYSTEM',
    title           VARCHAR(100) NOT NULL COMMENT '通知标题',
    content         VARCHAR(500) NOT NULL COMMENT '通知正文',
    -- 关联业务
    ref_type        VARCHAR(30)  NULL COMMENT '关联业务类型：DEMAND/TEAHOUSE',
    ref_id          BIGINT       NULL COMMENT '关联业务ID',
    -- 状态
    is_read         TINYINT      NOT NULL DEFAULT 0 COMMENT '0未读/1已读',
    -- 需要用户回复的场景
    need_action     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否需要用户操作确认',
    action_type     VARCHAR(30)  NULL COMMENT '操作类型：CONFIRM_COMPLETE/EXTEND_TEAHOUSE',
    action_done     TINYINT      NOT NULL DEFAULT 0 COMMENT '用户是否已操作',
    -- 时间
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_noti_user (user_id, is_read, created_at),
    INDEX idx_noti_ref (ref_type, ref_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='APP内通知表';
