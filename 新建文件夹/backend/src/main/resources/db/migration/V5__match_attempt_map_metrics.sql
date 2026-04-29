ALTER TABLE match_attempt
    ADD COLUMN IF NOT EXISTS distance_source VARCHAR(20) NULL,
    ADD COLUMN IF NOT EXISTS route_distance_km DECIMAL(8, 2) NULL,
    ADD COLUMN IF NOT EXISTS straight_distance_km DECIMAL(8, 2) NULL,
    ADD COLUMN IF NOT EXISTS eta_minutes INT NULL;
