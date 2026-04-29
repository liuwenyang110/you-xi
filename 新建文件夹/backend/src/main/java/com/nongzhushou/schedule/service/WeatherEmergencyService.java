package com.nongzhushou.schedule.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import com.nongzhushou.common.api.QWeatherClient;

/**
 * 灾害气象紧急抢收引擎 (实用主义核心：保量防灾)
 */
@Service
public class WeatherEmergencyService {

    private static final Logger log = LoggerFactory.getLogger(WeatherEmergencyService.class);

    @Autowired
    private QWeatherClient qWeatherClient;

    /**
     * 触发气象红色预警调度 (如暴雨将至)
     * 核心逻辑：打破原有的“价格优先/距离优先”，切换为“绝对聚集抢收模式”
     */
    public String triggerStormEvacuationDispatch(String warningRegionCode, double warningLat, double warningLng) {
        
        // --- 核心改动：接入真实的互联网 API 判断，不玩虚的 ---
        boolean isRealRedAlert = qWeatherClient.checkRedDisasterWarningNow(warningRegionCode);
        
        if (!isRealRedAlert) {
            log.info("和风天气 API 返回：目前 {} 无暴雨红色预警，维持普通联系调度", warningRegionCode);
            return "当前气象平稳，无需抢收。";
        }

        // 1. 发射全平台级通知：灾害模式已启动
        log.warn("[真实 API 触发警告] {} 即将遭遇极端天气，启动紧急抢收引擎！", warningRegionCode);

        // 2. 冻结所有该区域及其周边 30km 内尚未确认联系的普通预约槽位
        freezeNormalSchedules(warningRegionCode);

        // 3. 算法：全盘贪心调度，计算方圆 30km 内所有农机的 VRP 路径，强制导向受灾农田
        int dispatchedMachines = forceDispatchToDisasterZone(warningLat, warningLng, 30.0);

        return "灾害抢收指令下达！已强制征调集结周边 " + dispatchedMachines + " 台农机，平台将持续同步联系与作业进度。";
    }

    private void freezeNormalSchedules(String regionCode) {
        // Todo: 对接 Redis 锁，强制清理非必须的槽位
    }

    private int forceDispatchToDisasterZone(double lat, double lng, double radiusKm) {
        // Todo: 调用空间数据库寻找机器并推送信令，这里模拟返回调度数量
        return 47; 
    }
}
