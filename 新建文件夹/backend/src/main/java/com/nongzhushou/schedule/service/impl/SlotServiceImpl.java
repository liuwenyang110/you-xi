package com.nongzhushou.schedule.service.impl;

import com.nongzhushou.schedule.service.SlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 后端与算法专家联手 - 槽位排期服务实现类
 */
@Service
public class SlotServiceImpl implements SlotService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 高并发 Lua 脚本锁定槽位 (12306模式)
     */
    public boolean lockSlotWithLua(Long equipmentId, String date, Long slotMask, Long userId) {
        String script = 
            "local equip_key = KEYS[1] " +
            "local user_key  = KEYS[2] " +
            "local req_slot_mask = tonumber(ARGV[1]) " +
            "local user_id = tonumber(ARGV[2]) " +
            "if redis.call('EXISTS', user_key) == 1 then return -1 end " +
            "local remaining = tonumber(redis.call('HGET', equip_key, 'remaining')) " +
            "if not remaining or remaining <= 0 then return -3 end " +
            "redis.call('HINCRBY', equip_key, 'remaining', -1) " +
            "redis.call('SETEX', user_key, 900, equip_key) " +
            "return 1";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);

        String equipKey = "slot_inventory:" + equipmentId + ":" + date;
        String userKey = "user_lock:" + userId;
        List<String> keys = Arrays.asList(equipKey, userKey);

        Long result = redisTemplate.execute(redisScript, keys, slotMask.toString(), userId.toString());

        return result != null && result == 1;
    }
}
