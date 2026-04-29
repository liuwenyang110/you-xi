ALTER TABLE equipment
    ADD COLUMN current_lat DECIMAL(10, 6) NULL,
    ADD COLUMN current_lng DECIMAL(10, 6) NULL;

UPDATE equipment
SET current_lat = COALESCE(current_lat, 30.274150),
    current_lng = COALESCE(current_lng, 120.155150)
WHERE id = 1001;
