-- =============================================
-- V12: 需求生命周期追踪字段
-- 支撑 T+2首问 / T+4二问 / 最后通牒 / T+5强杀 机制
-- =============================================

ALTER TABLE v3_demand
    ADD COLUMN first_ask_at     DATETIME     NULL COMMENT '第一次询问发送时间（T+2天）',
    ADD COLUMN first_ask_reply  VARCHAR(20)  NULL COMMENT '第一次回复：ONGOING进行中/COMPLETED已完成/NULL未理',
    ADD COLUMN second_ask_at    DATETIME     NULL COMMENT '第二次询问发送时间（T+4天）',
    ADD COLUMN second_ask_reply VARCHAR(20)  NULL COMMENT '第二次回复',
    ADD COLUMN final_warn_at    DATETIME     NULL COMMENT '最后通牒发送时间',
    ADD COLUMN auto_cleaned     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否被系统强制清理 1=是';

-- 为定时任务扫描创建索引
CREATE INDEX idx_v3_demand_lifecycle ON v3_demand (status, published_at, first_ask_at, second_ask_at, final_warn_at);
