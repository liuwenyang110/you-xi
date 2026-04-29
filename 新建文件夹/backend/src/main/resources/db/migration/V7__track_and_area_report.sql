-- V7: 轨迹记录表 + 面积报告表
-- 用于 GPS 轨迹上报与作业面积自动计算（公益版：仅供参考，不涉及结算）

CREATE TABLE IF NOT EXISTS nong_track_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '关联订单ID',
    owner_id BIGINT NOT NULL COMMENT '上报机手ID',
    lat DECIMAL(10, 7) NOT NULL COMMENT '纬度',
    lng DECIMAL(10, 7) NOT NULL COMMENT '经度',
    speed FLOAT NULL COMMENT '速度 m/s',
    accuracy FLOAT NULL COMMENT 'GPS精度 m',
    device_time BIGINT NULL COMMENT '设备端时间戳(毫秒)',
    is_corrected TINYINT NOT NULL DEFAULT 0 COMMENT '是否经过滤波纠偏 0否 1是',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_track_order (order_id),
    INDEX idx_track_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业轨迹点记录表';

CREATE TABLE IF NOT EXISTS area_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL UNIQUE COMMENT '关联订单ID',
    owner_id BIGINT NOT NULL,
    farmer_id BIGINT NOT NULL,
    track_point_count INT NOT NULL DEFAULT 0 COMMENT '有效轨迹点数',
    raw_area_mu DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '原始计算亩数',
    corrected_area_mu DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '纠偏后亩数',
    work_width_m DECIMAL(5, 2) NOT NULL DEFAULT 2.20 COMMENT '作业幅宽(米)',
    total_distance_m DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '总行驶距离(米)',
    work_duration_minutes INT NOT NULL DEFAULT 0 COMMENT '作业时长(分钟)',
    signal_quality VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN' COMMENT '信号质量: GOOD/FAIR/POOR/UNKNOWN',
    status VARCHAR(20) NOT NULL DEFAULT 'GENERATED' COMMENT 'GENERATED/DISPUTED/CONFIRMED',
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_report_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业面积报告表';
