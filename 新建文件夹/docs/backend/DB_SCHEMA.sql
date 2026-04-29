CREATE TABLE user_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone VARCHAR(20) NOT NULL UNIQUE,
    primary_role VARCHAR(20) NOT NULL,
    current_role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_real_auth (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    id_card_no VARCHAR(32) NOT NULL,
    auth_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE service_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0,
    category_name VARCHAR(100) NOT NULL,
    category_code VARCHAR(50) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    enabled TINYINT NOT NULL DEFAULT 1
);

CREATE TABLE machine_type_dict (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    machine_name VARCHAR(100) NOT NULL,
    horsepower_range VARCHAR(50),
    enabled TINYINT NOT NULL DEFAULT 1
);

CREATE TABLE dynamic_field_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_type VARCHAR(20) NOT NULL,
    ref_id BIGINT NOT NULL,
    field_key VARCHAR(50) NOT NULL,
    field_label VARCHAR(100) NOT NULL,
    field_type VARCHAR(30) NOT NULL,
    required_flag TINYINT NOT NULL DEFAULT 0,
    options_json JSON NULL
);

CREATE TABLE equipment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    machine_type_id BIGINT NOT NULL,
    equipment_name VARCHAR(100) NOT NULL,
    brand_model VARCHAR(100),
    quantity INT NOT NULL DEFAULT 1,
    current_status VARCHAR(20) NOT NULL DEFAULT 'IDLE',
    base_region_code VARCHAR(32),
    service_radius_km INT DEFAULT 15,
    approve_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE demand (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    farmer_id BIGINT NOT NULL,
    service_category_id BIGINT NOT NULL,
    service_subcategory_id BIGINT NOT NULL,
    crop_code VARCHAR(50),
    area_mu DECIMAL(10, 2),
    schedule_type VARCHAR(20),
    village_name VARCHAR(100),
    lat DECIMAL(10, 6),
    lng DECIMAL(10, 6),
    voice_asset_id BIGINT,
    voice_text VARCHAR(1000),
    requirement_json JSON,
    status VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- V2: 补贴申领记录 (PM 视角：政策对接)
CREATE TABLE subsidy_claim (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    farmer_id BIGINT NOT NULL,
    demand_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    estimated_subsidy DECIMAL(10, 2),
    actual_subsidy DECIMAL(10, 2),
    claim_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', -- DRAFT, SUBMITTED, APPROVED, PAID
    evidence_json JSON, -- 存证照片、地理位置、作业证明
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- V2: 村落信用/信任评分 (PM 视角：场景深挖)
CREATE TABLE village_trust_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    target_id BIGINT NOT NULL, -- user_id 或 equipment_id
    target_type VARCHAR(20) NOT NULL, -- FARMER, OWNER, EQUIPMENT
    score DECIMAL(5, 2) DEFAULT 5.0,
    recommend_count INT DEFAULT 0,
    village_code VARCHAR(32),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- V2: 农机排期表 (Algorithm 视角：12306级库存锁定)
CREATE TABLE equipment_timetable (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    equipment_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    time_slot_mask BIGINT NOT NULL, -- 位掩码表示时间段锁定情况
    locked_units INT NOT NULL DEFAULT 0,
    total_units INT NOT NULL DEFAULT 1,
    INDEX idx_equip_date (equipment_id, work_date)
);
