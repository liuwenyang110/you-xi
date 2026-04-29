-- 由底层架构师（AI）设计的性能与并发双驱优化脚本
-- 添加高频查询覆盖索引，防止回表性能雪崩
-- 增加乐观锁控制字段 (version) 确保状态机的严格正向演进

-- 1. 核心订单表：订单查询高发于：用户查询自己订单、机主查询名下订单，且常常附带状态过滤与时间排序。
ALTER TABLE order_info
    ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，防止高并发踩踏',
    ADD INDEX idx_farmer_status_time (farmer_id, status, created_at),
    ADD INDEX idx_owner_status_time (owner_id, status, created_at);

-- 2. 农机装备表：解决 Geo Hash 兜底或管理后台按状态查、按属地查的慢 SQL 隐患。
ALTER TABLE equipment
    ADD INDEX idx_status_loc (current_status, current_lng, current_lat),
    ADD INDEX idx_base_region (base_region_code, current_status);

-- 3. 需求表：农户查自己需求列表的高频接口、派单兜底扫描未被接单的查询
ALTER TABLE demand
    ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT '需求状态变更的乐观锁',
    ADD INDEX idx_farmer_status (farmer_id, status, created_at),
    ADD INDEX idx_status_published (status, published_at);

-- 4. 并发意向匹配表：清理超时时效性的高发定时扫表
ALTER TABLE match_attempt
    ADD INDEX idx_status_deadline (status, response_deadline_seconds, created_at);
