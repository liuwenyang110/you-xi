ALTER TABLE demand
    ADD COLUMN expected_date DATE NULL,
    ADD COLUMN remark VARCHAR(255) NULL,
    ADD COLUMN published_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN match_attempt_count INT NOT NULL DEFAULT 0,
    ADD COLUMN current_match_attempt_id BIGINT NULL,
    ADD COLUMN current_contact_session_id BIGINT NULL,
    ADD COLUMN current_order_id BIGINT NULL;

CREATE TABLE IF NOT EXISTS service_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    service_category_id BIGINT NOT NULL,
    service_subcategory_id BIGINT NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    machine_binding_type VARCHAR(20) NOT NULL,
    main_equipment_id BIGINT NOT NULL,
    related_equipment_ids VARCHAR(255),
    crop_tags VARCHAR(255),
    terrain_tags VARCHAR(255),
    plot_tags VARCHAR(255),
    min_area_mu DECIMAL(10, 2),
    max_area_mu DECIMAL(10, 2),
    available_time_desc VARCHAR(100),
    is_accepting_orders TINYINT NOT NULL DEFAULT 1,
    service_radius_km INT NOT NULL DEFAULT 15,
    approve_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS match_attempt (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    demand_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    service_item_id BIGINT NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    village_name VARCHAR(100),
    distance_layer VARCHAR(20),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_RESPONSE',
    response_deadline_seconds INT NOT NULL DEFAULT 120,
    handled_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS contact_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    demand_id BIGINT NOT NULL,
    match_attempt_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    farmer_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'WAITING_FARMER_CONFIRM',
    active_flag TINYINT NOT NULL DEFAULT 1,
    masked_phone VARCHAR(32),
    confirmed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    demand_id BIGINT NOT NULL,
    match_attempt_id BIGINT NOT NULL,
    contact_session_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    farmer_id BIGINT NOT NULL,
    service_item_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_CONTACT',
    owner_confirmed_finish TINYINT NOT NULL DEFAULT 0,
    farmer_confirmed_finish TINYINT NOT NULL DEFAULT 0,
    contact_confirmed_at DATETIME NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS report_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reporter_id BIGINT NOT NULL,
    reported_user_id BIGINT NOT NULL,
    order_id BIGINT NULL,
    demand_id BIGINT NULL,
    report_type VARCHAR(30) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    evidence_asset_ids VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin_audit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    action VARCHAR(50) NOT NULL,
    content VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO user_account (id, phone, primary_role, current_role, status)
SELECT 10001, '13800000001', 'FARMER', 'FARMER', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM user_account WHERE id = 10001);

INSERT INTO user_account (id, phone, primary_role, current_role, status)
SELECT 10002, '13800000002', 'OWNER', 'OWNER', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM user_account WHERE id = 10002);

INSERT INTO user_account (id, phone, primary_role, current_role, status)
SELECT 10003, '13800000003', 'ADMIN', 'ADMIN', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM user_account WHERE id = 10003);

INSERT INTO equipment (id, owner_id, machine_type_id, equipment_name, brand_model, quantity, current_status, base_region_code, service_radius_km, approve_status)
SELECT 1001, 10002, 102, 'Crawler Harvester', '4LZ-4.0', 1, 'IDLE', '330106001001', 15, 'PASSED'
WHERE NOT EXISTS (SELECT 1 FROM equipment WHERE id = 1001);

INSERT INTO service_item (id, owner_id, service_category_id, service_subcategory_id, service_name, machine_binding_type, main_equipment_id, related_equipment_ids, crop_tags, terrain_tags, plot_tags, min_area_mu, max_area_mu, available_time_desc, is_accepting_orders, service_radius_km, approve_status, status)
SELECT 2001, 10002, 3, 301, 'Rice Harvest Service', 'single', 1001, '1001', 'RICE', 'PLAIN,WET_FIELD', 'SMALL_PLOT', 1, 80, 'Available today', 1, 15, 'PASSED', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE id = 2001);
