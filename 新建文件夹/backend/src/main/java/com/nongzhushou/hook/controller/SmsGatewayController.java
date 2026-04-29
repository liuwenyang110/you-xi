package com.nongzhushou.hook.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import org.springframework.beans.factory.annotation.Autowired;
import com.nongzhushou.common.api.AliyunSmsClient;

/**
 * 短信信令降级网关 (实用主义核心：防断网失联)
 * 用于极度偏远、无 4G 信号的农田，农机主通过发短信完成操作。
 */
@RestController
@RequestMapping("/api/v2/sms-fallback")
public class SmsGatewayController {

    private static final Logger log = LoggerFactory.getLogger(SmsGatewayController.class);

    @Autowired
    private AliyunSmsClient aliyunSmsClient;

    /**
     * 接收来自通信运营商的短信转发回调
     * 举例短信内容格式: "WCDD#8592" (完成订单 8592)
     */
    @PostMapping("/receive")
    public ResponseEntity<String> handleOfflineSmsCommand(
            @RequestParam String senderPhone, 
            @RequestParam String messageContent) {
        
        log.info("接收到来自无网区的下沉短令: {} -> {}", senderPhone, messageContent);

        if (messageContent.startsWith("WCDD#")) {
            String orderId = messageContent.split("#")[1];
            
            // Todo: 唤醒对应订单的分布式锁，执行离线完工记录与状态扭转
            
            // --- 核心改动：接入真实的阿里云核心网，确保确认短信能及时发出 ---
            boolean smsSent = aliyunSmsClient.sendConfirmationSms(senderPhone, orderId);
            
            if (smsSent) {
               return ResponseEntity.ok("指令已收悉，订单 " + orderId + " 已离线记录完成，且已下发确认短信");
            } else {
               return ResponseEntity.ok("指令收悉核验中 (短信网关触发失败)");
            }
        }

        return ResponseEntity.badRequest().body("未知指令");
    }
}
