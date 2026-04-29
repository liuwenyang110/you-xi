-- =============================================
-- V10: 平台定位调整 — 从"交易闭环"转型为"联系对接"
-- 核心理念：做减法，去复杂交易流程，强化联系与互助
-- =============================================

-- -----------------------------------------------
-- 1. contact_session 升级为核心表
--    新增联系类型、服务反馈、评价字段
-- -----------------------------------------------
ALTER TABLE contact_session
    ADD COLUMN contact_type VARCHAR(20) NULL DEFAULT 'PHONE'
        COMMENT '联系方式: PHONE=电话 / WECHAT=微信 / VISIT=上门',
    ADD COLUMN farmer_feedback VARCHAR(500) NULL
        COMMENT '农户对本次服务的反馈',
    ADD COLUMN farmer_rating TINYINT NULL
        COMMENT '农户评分 1-5 星',
    ADD COLUMN service_completed TINYINT NOT NULL DEFAULT 0
        COMMENT '服务是否已完成 0否 1是',
    ADD COLUMN discovery_source VARCHAR(30) NULL DEFAULT 'MATCH'
        COMMENT '联系发现来源: MATCH=匹配 / BROWSE=浏览 / POST=社区帖子 / SHARE=分享';

-- -----------------------------------------------
-- 2. demand 表简化
--    保留 current_contact_session_id
--    标记匹配关联字段为非必需（不物理删除，保留向后兼容）
-- -----------------------------------------------
ALTER TABLE demand
    ADD COLUMN discovery_source VARCHAR(30) NULL DEFAULT 'BROWSE'
        COMMENT '需求发现方式: BROWSE=自主浏览 / POST=社区帖子 / SHARE=分享链接 / VOLUNTEER=志愿认领';

-- -----------------------------------------------
-- 3. user_account 扩展微信生态字段
--    支持微信小程序登录和用户画像
-- -----------------------------------------------
ALTER TABLE user_account
    ADD COLUMN wx_openid VARCHAR(64) NULL
        COMMENT '微信小程序 openid',
    ADD COLUMN wx_unionid VARCHAR(64) NULL
        COMMENT '微信开放平台 unionid（跨应用统一标识）',
    ADD COLUMN avatar_url VARCHAR(500) NULL
        COMMENT '用户头像URL',
    ADD COLUMN nickname VARCHAR(50) NULL
        COMMENT '用户昵称';

-- -----------------------------------------------
-- 4. 新增隐私合规表
--    满足《个人信息保护法》用户同意记录要求
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS privacy_consent (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT        NOT NULL COMMENT '用户ID',
    consent_type  VARCHAR(30)   NOT NULL COMMENT '同意类型: PRIVACY_POLICY / LOCATION / PHONE_DISPLAY',
    consented     TINYINT       NOT NULL DEFAULT 0 COMMENT '是否同意 0否 1是',
    consent_version VARCHAR(20) NULL COMMENT '同意的隐私政策版本号',
    consented_at  DATETIME      NULL COMMENT '同意时间',
    ip_address    VARCHAR(45)   NULL COMMENT '同意时的IP地址',
    user_agent    VARCHAR(500)  NULL COMMENT '同意时的设备信息',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_privacy_user (user_id),
    INDEX idx_privacy_type (consent_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户隐私同意记录表（PIPL合规）';

-- -----------------------------------------------
-- 5. 新增信誉等级表（老把式认证体系）
--    复用 village_trust_score 做底层分，另加等级映射
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS reputation_level (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL UNIQUE COMMENT '用户ID',
    level_code    VARCHAR(20)  NOT NULL DEFAULT 'NEWBIE' COMMENT 'NEWBIE/RELIABLE/VETERAN/GOLD/MODEL',
    level_name    VARCHAR(20)  NOT NULL DEFAULT '新手机手' COMMENT '中文等级名',
    total_services INT        NOT NULL DEFAULT 0 COMMENT '累计服务次数',
    good_rate     DECIMAL(5,2) NOT NULL DEFAULT 100.00 COMMENT '好评率',
    has_disaster_record TINYINT NOT NULL DEFAULT 0 COMMENT '是否参与过救灾',
    upgraded_at   DATETIME     NULL COMMENT '最近升级时间',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_reputation_level (level_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老把式信誉等级表';
