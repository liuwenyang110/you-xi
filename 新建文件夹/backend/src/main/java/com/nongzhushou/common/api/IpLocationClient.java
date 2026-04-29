package com.nongzhushou.common.api;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 太平洋电脑网免 Key IP 分析挂载网关
 * 专为政府大屏地理热力图提供免鉴权支持
 */
@Component
public class IpLocationClient {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 将请求侧真实 IP 打向太平洋验证网关，抓取出省市地理标志
     * @param ip 用户请求的真实外网IP，例如 "113.111.xxx.xxx"
     * @return 包含省市名称的结构化信息，比如 "广东省 广州市"
     */
    public String resolveProvinceAndCity(String ip) {
        if (ip == null || ip.isEmpty() || "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            // 本地调试的默认模拟地址
            return "开发机器 本地调试";
        }

        try {
            // 太平洋老牌且非常稳定的免费、免 Key 查询口：带 json=true 则返回规范数据
            String url = String.format("https://whois.pconline.com.cn/ipJson.jsp?ip=%s&json=true", ip);
            String responseStr = restTemplate.getForObject(url, String.class);

            if (responseStr != null) {
                // pconline 在低版本 Spring 中可能会抛出 GBK 中文乱码，此处如果乱码后续可增加 StringHttpMessageConverter 排期处理
                JSONObject json = JSON.parseObject(responseStr.trim());
                String province = json.getString("pro");
                String city = json.getString("city");
                
                // 返回拼接后的区域。若太平洋未收录，则会返回空字符串
                if (province != null && !province.isEmpty()) {
                     return province + " " + (city != null ? city : "");
                }
            }
        } catch (Exception e) {
            System.err.println("太平洋公益 IP 节点请求超时或解析失败：" + e.getMessage());
        }
        
        return "未知定位区";
    }
}
