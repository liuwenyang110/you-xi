-- =============================================
-- V9: 体验审计增补字段 (语音/答谢/地况)
-- =============================================

ALTER TABLE demand
    ADD COLUMN gratitude_type VARCHAR(50) NULL COMMENT '答谢方式，如：WATER, SMOKE, MEAL, POINTS',
    ADD COLUMN plot_tags VARCHAR(255) NULL COMMENT '地块路况，逗号分隔，如：PLAIN,MUDDY,NARROW';
