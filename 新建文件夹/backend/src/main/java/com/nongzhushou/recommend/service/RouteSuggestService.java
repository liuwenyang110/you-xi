package com.nongzhushou.recommend.service;

import com.nongzhushou.v3demand.mapper.V3DemandMapper;
import com.nongzhushou.zone.entity.ZoneEntity;
import com.nongzhushou.zone.mapper.ZoneMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 顺路推荐服务
 *
 * 当前策略：基于 township_code 的同镇村匹配
 * 未来可通过 SPI 接口替换为高德地图真实路线推荐
 */
@Service
public class RouteSuggestService {

    private static final Logger log = LoggerFactory.getLogger(RouteSuggestService.class);

    @Autowired private V3DemandMapper demandMapper;
    @Autowired private ZoneMapper zoneMapper;

    /**
     * 顺路推荐：机手选定目标村后，返回同镇其他村的匹配需求
     * @param targetZoneId 机手要去的村
     * @param myZoneId     机手当前所在村
     * @return 沿途推荐列表
     */
    public List<Map<String, Object>> suggestRoute(Long targetZoneId, Long myZoneId) {
        // 获取目标村的镇级编码
        ZoneEntity targetZone = zoneMapper.selectById(targetZoneId);
        if (targetZone == null || targetZone.getTownshipCode() == null) {
            return Collections.emptyList();
        }
        String townshipCode = targetZone.getTownshipCode();

        // 获取同镇所有村的需求气泡聚合
        List<Map<String, Object>> allBubbles = demandMapper.aggregateByTownship(townshipCode);

        // 过滤掉目标村和自己所在的村，按需求量降序
        return allBubbles.stream()
            .filter(b -> {
                Object zid = b.get("zoneId");
                if (zid == null) return false;
                long id = Long.parseLong(zid.toString());
                return id != targetZoneId && id != myZoneId;
            })
            .sorted((a, b) -> {
                int ca = Integer.parseInt(a.getOrDefault("demandCount", 0).toString());
                int cb = Integer.parseInt(b.getOrDefault("demandCount", 0).toString());
                return cb - ca;
            })
            .limit(5)
            .collect(Collectors.toList());
    }

    /**
     * 跨镇悬赏广播：检测某镇的运力是否紧缺
     * 如果某镇的活跃需求数 / 机手数 > 3（即平均每人超过3单），触发悬赏
     * @return 需要悬赏的镇列表
     */
    public List<Map<String, Object>> checkCrossTownBounty() {
        // 获取所有镇的统计
        List<Map<String, Object>> townStats = zoneMapper.townshipCapacityStats();
        List<Map<String, Object>> needBounty = new ArrayList<>();

        for (Map<String, Object> stat : townStats) {
            int demands = Integer.parseInt(stat.getOrDefault("demandCount", 0).toString());
            int operators = Integer.parseInt(stat.getOrDefault("operatorCount", 1).toString());
            if (operators == 0) operators = 1;

            double ratio = (double) demands / operators;
            if (ratio > 3.0 && demands > 5) {
                stat.put("ratio", ratio);
                stat.put("urgency", ratio > 5 ? "HIGH" : "MEDIUM");
                needBounty.add(stat);
            }
        }
        return needBounty;
    }
}
