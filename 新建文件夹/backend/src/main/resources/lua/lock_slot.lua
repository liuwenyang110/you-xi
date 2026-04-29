-- 由数据库算法专家出具 - 高并发槽位锁定极其回滚 Lua 脚本 (12306模式)
-- 支持正向 LOCK 和逆向补偿 UNLOCK（超时未回滚的容错）

local equip_key = KEYS[1]       -- Redis中的库存Key: slot_inventory:{equip_id}:{date}
local user_key  = KEYS[2]       -- Redis中防提重的Key: user_lock:{user_id}

local op = ARGV[1]              -- 操作类型: "LOCK" 或 "UNLOCK"
local req_amount = tonumber(ARGV[2] or 1) -- 需要数量

if op == "UNLOCK" then
    -- 服务端监听延时队列/死信队列触发，或者客户端超时取消导致
    -- 归还产能：
    redis.call("HINCRBY", equip_key, "remaining", req_amount)
    -- 移除抢单互斥：
    if redis.call("GET", user_key) == equip_key then
        redis.call("DEL", user_key)
    end
    return 1
end

-- 以下为 "LOCK" 逻辑：
-- 1. 检查用户是否在前次抢单处于锁定且待确认联系态 (限制一人一单防黄牛)
if redis.call("EXISTS", user_key) == 1 then
    return -1 -- 错误码: 频繁操作或已有待处理流转单
end

-- 2. 获取并检查剩余槽位是否满足
local remaining_capacity = tonumber(redis.call("HGET", equip_key, "remaining") or 0)
if remaining_capacity < req_amount then
    return -3 -- 错误码: 当前槽位已抢空或者不足以承接
end

-- 3. 执行扣减并占位
redis.call("HINCRBY", equip_key, "remaining", -req_amount)
redis.call("SETEX", user_key, 900, equip_key) -- 默认锁定排队窗口15分钟 (900s)

return 1
