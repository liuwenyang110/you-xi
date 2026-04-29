-- =============================================
-- V8: 社区帖子 + 农机租赁 + 志愿认领
-- =============================================

-- 1. 社区帖子表 (承载找机急单、闲置出租、农技互动三种卡片)
CREATE TABLE IF NOT EXISTS community_post (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    author_id      BIGINT       NOT NULL COMMENT '发布人 user_account.id',
    post_type      VARCHAR(30)  NOT NULL COMMENT 'DEMAND_URGENT / RENTAL / CHAT',
    title          VARCHAR(200) NOT NULL,
    content        TEXT,
    images_json    JSON         NULL     COMMENT '图片URL列表 ["url1","url2"]',
    crop_code      VARCHAR(50)  NULL,
    machine_category VARCHAR(30) NULL    COMMENT 'taxonomy 机械大类 TILLAGE/PLANTING/MANAGEMENT/HARVEST',
    machine_type   VARCHAR(50)  NULL     COMMENT 'taxonomy 具体型号 COMBINE_HARVESTER 等',
    area_mu        DECIMAL(10,2) NULL,
    lat            DECIMAL(10,6) NULL,
    lng            DECIMAL(10,6) NULL,
    location_name  VARCHAR(200) NULL,
    is_urgent      TINYINT      NOT NULL DEFAULT 0,
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / CLOSED / DELETED',
    view_count     INT          NOT NULL DEFAULT 0,
    reply_count    INT          NOT NULL DEFAULT 0,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_post_type (post_type),
    INDEX idx_author (author_id),
    INDEX idx_created (created_at DESC)
);

-- 2. 农机租赁信息表 (由社区帖子 post_type=RENTAL 时关联)
CREATE TABLE IF NOT EXISTS machinery_rental (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id        BIGINT        NOT NULL COMMENT '出租人 user_account.id',
    post_id         BIGINT        NULL     COMMENT '关联的社区帖子',
    machine_category VARCHAR(30)  NOT NULL,
    machine_type    VARCHAR(50)   NOT NULL,
    brand_model     VARCHAR(100)  NOT NULL COMMENT '品牌型号，如 东方红 120马力',
    price_mode      VARCHAR(20)   NOT NULL COMMENT 'PER_DAY / PER_MU / DISCUSS',
    price_value     DECIMAL(10,2) NULL     COMMENT '面议时为 NULL',
    images_json     JSON          NULL,
    location_name   VARCHAR(200)  NULL,
    lat             DECIMAL(10,6) NULL,
    lng             DECIMAL(10,6) NULL,
    contact_phone   VARCHAR(20)   NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE / RENTED / OFF',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_owner (owner_id),
    INDEX idx_status (status)
);

-- 3. 志愿认领记录表 (替代商业化的"抢单")
CREATE TABLE IF NOT EXISTS volunteer_claim (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    demand_id       BIGINT       NOT NULL COMMENT '关联需求',
    post_id         BIGINT       NULL     COMMENT '关联的社区急单帖子(可选)',
    volunteer_id    BIGINT       NOT NULL COMMENT '认领的机手 user_account.id',
    equipment_id    BIGINT       NULL     COMMENT '使用的农机',
    status          VARCHAR(30)  NOT NULL DEFAULT 'CLAIMED' COMMENT 'CLAIMED / EN_ROUTE / WORKING / FINISHED / CANCELLED',
    claimed_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    arrived_at      DATETIME     NULL,
    finished_at     DATETIME     NULL,
    area_report_id  BIGINT       NULL     COMMENT '关联GPS面积报告',
    farmer_feedback VARCHAR(500) NULL,
    farmer_rating   TINYINT      NULL     COMMENT '1-5 星',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_demand (demand_id),
    INDEX idx_volunteer (volunteer_id)
);

-- 4. 社区帖子回复表
CREATE TABLE IF NOT EXISTS community_reply (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id     BIGINT       NOT NULL,
    author_id   BIGINT       NOT NULL,
    content     VARCHAR(1000) NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post (post_id)
);
