ALTER TABLE contact_session
    ADD COLUMN session_type VARCHAR(30) NULL DEFAULT 'PUBLIC_GOOD',
    ADD COLUMN source_post_id BIGINT NULL,
    ADD COLUMN volunteer_claim_id BIGINT NULL,
    ADD COLUMN subject VARCHAR(200) NULL,
    ADD COLUMN summary VARCHAR(500) NULL,
    ADD COLUMN delivery_mode VARCHAR(20) NULL DEFAULT 'POLLING',
    ADD COLUMN last_message_preview VARCHAR(255) NULL,
    ADD COLUMN last_sender_id BIGINT NULL,
    ADD COLUMN unread_farmer_count INT NOT NULL DEFAULT 0,
    ADD COLUMN unread_owner_count INT NOT NULL DEFAULT 0,
    ADD COLUMN last_message_at DATETIME NULL,
    ADD COLUMN last_ack_at DATETIME NULL;

CREATE TABLE IF NOT EXISTS collab_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    sender_role VARCHAR(20) NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    content VARCHAR(2000) NULL,
    media_url VARCHAR(500) NULL,
    system_flag TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_collab_message_session (session_id),
    INDEX idx_collab_message_created (created_at)
);

CREATE TABLE IF NOT EXISTS collab_session_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_detail VARCHAR(500) NULL,
    operator_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_collab_event_session (session_id),
    INDEX idx_collab_event_created (created_at)
);
