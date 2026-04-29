-- 数据库/算法专家(Gemini High)定制 - GeoHash 附近农机 O(1) 搜索与锁单 Lua 并发控制
-- 设计原则来源于 @skills/redis-lua-concurrency 
-- KEYS[1] = "equip_locations" (存储所有农机的 GeoSet key)
-- KEYS[2] = "user_lock:" .. user_id (用户防重锁)
-- ARGV[1] = 农户纬度 (lat)
-- ARGV[2] = 农户经度 (lng)
-- ARGV[3] = 搜索半径公里数 (e.g. 5)
-- ARGV[4] = 所需容量/槽位需求 (目前简化为数量1)
-- ARGV[5] = user_id
-- ARGV[6] = 预约日期 (date string, e.g. "2026-04-19")

-- [降级与高可用规范补充]
-- 1. 当 Redis 宕机时，Java 业务层将自动进行熔断并回放为 DB 直接匹配。
-- 2. DB 退化抢单将使用 update order_info set version = version + 1 where id=? 乐观锁执行并发控制。
-- 3. 本脚本提供原子锁定功能，过期 TTL 处理通过 lock_slot 中的 UNLOCK 操作解耦或 DB 时钟轮询退回。

local geo_key = KEYS[1]
local user_key = KEYS[2]

local lat = tonumber(ARGV[1])
local lng = tonumber(ARGV[2])
local radius = tonumber(ARGV[3])
local req_amount = tonumber(ARGV[4])
local user_id = ARGV[5]
local date_str = ARGV[6]

-- 1. 检查用户是否已锁单 (防止刷单黄牛)
if redis.call("EXISTS", user_key) == 1 then
    return {-1, "USER_ALREADY_LOCKED"}
end

-- 2. 搜索附近的农机装备 (优先 Redis 6.2+ 的 GEOSEARCH)
-- 返回格式: {equip_id1, equip_id2, ...} (距离由近到远，符合就近派发原则)
local near_equips
local status, res = pcall(function() 
    return redis.call("GEOSEARCH", geo_key, "FROMLONLAT", lng, lat, "BYRADIUS", radius, "km", "ASC")
end)

if not status then
    -- 如果生产环境 Redis 小于 6.2，降级为 GEORADIUS 
    near_equips = redis.call("GEORADIUS", geo_key, lng, lat, radius, "km", "ASC")
else
    near_equips = res
end

-- 如果范围内没有任何注册的农机
if #near_equips == 0 then
    return {-2, "NO_EQUIPMENT_NEARBY"}
end

-- 3. 遍历就近农机，尝试并发安全地扣减库存锁单 (参照 12306 余票模型)
for i, equip_id in ipairs(near_equips) do
    local equip_inventory_key = "slot_inventory:" .. equip_id .. ":" .. date_str
    
    local remaining = tonumber(redis.call("HGET", equip_inventory_key, "remaining") or 0)
    if remaining >= req_amount then
        -- 尝试扣减槽位
        redis.call("HINCRBY", equip_inventory_key, "remaining", -req_amount)
        
        -- 给用户挂上这台机器的独占锁（待确认联系占位，15分钟过期）
        redis.call("SETEX", user_key, 900, equip_inventory_key)
        
        -- 记录分配给农户的设备编号，通知 SpringBoot 继续走 MySQL 状态机持久化
        return {1, equip_id} 
    end
end

-- 搜到了农机，但全都没档期了（触发补偿降级到扩大圈层）
return {-3, "ALL_NEARBY_EQUIPMENT_BUSY"}
