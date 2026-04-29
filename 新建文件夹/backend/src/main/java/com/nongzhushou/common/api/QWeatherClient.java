package com.nongzhushou.common.api;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 真实外部 API: 和风天气灾害雷达接入
 * 替换掉之前的 "System.out.println" 和假预警
 */
@Component
public class QWeatherClient {

    private static final Logger log = LoggerFactory.getLogger(QWeatherClient.class);

    // 留出变量口：只需要外部 .env 投喂一个真实的 API Key 就能启动
    @Value("${qweather.api.key:your_qweather_key_here}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 调用和风天气的真实 HTTP 接口，检索指定位置当前是否处于红色预警状态
     * @param locationCode 地区代号，例如 101010100 (北京)
     * @return 如果有红色级别的暴雨、冰雹等灾害警告，返回 true
     */
    public boolean checkRedDisasterWarningNow(String locationCode) {
        if ("your_qweather_key_here".equals(apiKey)) {
            log.warn("尚未配置真实 QWeather Key，跳过真实请求");
            return false;
        }

        String url = String.format("https://devapi.qweather.com/v7/warning/now?location=%s&key=%s", locationCode, apiKey);
        
        try {
            String response = restTemplate.getForObject(url, String.class);
            JSONObject json = JSON.parseObject(response);

            // 解析和风天气的 JSON 结构
            if ("200".equals(json.getString("code"))) {
                var warnings = json.getJSONArray("warning");
                if (warnings != null && !warnings.isEmpty()) {
                    for (int i = 0; i < warnings.size(); i++) {
                        JSONObject w = warnings.getJSONObject(i);
                        String severityColor = w.getString("severityColor");
                        // 检索到 Red (红色) 级别的预警，立即触发最高全县抢收！
                        if ("Red".equalsIgnoreCase(severityColor)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            log.error("和风天气 API 调用失败 [{}]: {}", locationCode, e.getMessage());
            return false;
        }
    }
}
