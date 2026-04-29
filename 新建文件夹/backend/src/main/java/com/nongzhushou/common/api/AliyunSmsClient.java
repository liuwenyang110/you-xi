package com.nongzhushou.common.api;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 真实外部 API: 阿里云 SMS 官方 SDK 接入
 * 1. 登录验证码发送 (sendVerificationCode)
 * 2. 弱网离线订单核销回执 (sendConfirmationSms)
 */
@Component
public class AliyunSmsClient {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsClient.class);

    @Value("${aliyun.sms.access-key:}")
    private String accessKeyId;

    @Value("${aliyun.sms.secret-key:}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name:农助手}")
    private String signName;

    @Value("${aliyun.sms.template-code:}")
    private String templateCode;

    /** 验证码模板 ID（需在阿里云控制台申请，示例变量: ${code}） */
    @Value("${aliyun.sms.verify-template-code:}")
    private String verifyTemplateCode;

    private volatile Client cachedClient;

    @PostConstruct
    public void init() {
        if (isConfigured()) {
            log.info("✅ 阿里云短信服务已配置 (AK={}****)", accessKeyId.substring(0, Math.min(8, accessKeyId.length())));
        } else {
            log.warn("⚠️ 阿里云短信服务未配置 AKSK，真实短信发送能力不可用");
        }
    }

    /**
     * 判断 AKSK 是否已配置
     */
    public boolean isConfigured() {
        return accessKeyId != null && !accessKeyId.isBlank()
                && accessKeySecret != null && !accessKeySecret.isBlank();
    }

    /**
     * 懒加载并缓存 Client 实例
     */
    private Client getClient() throws Exception {
        if (cachedClient == null) {
            synchronized (this) {
                if (cachedClient == null) {
                    Config config = new Config()
                            .setAccessKeyId(accessKeyId)
                            .setAccessKeySecret(accessKeySecret);
                    config.endpoint = "dysmsapi.aliyuncs.com";
                    cachedClient = new Client(config);
                }
            }
        }
        return cachedClient;
    }

    /**
     * 发送登录验证码短信
     * @param phoneNumber 手机号
     * @param code 6位验证码
     * @return true=发送成功
     */
    public boolean sendVerificationCode(String phoneNumber, String code) {
        if (!isConfigured()) {
            log.warn("AKSK 未配置，跳过验证码发送 -> {}", phoneNumber);
            return false;
        }
        if (verifyTemplateCode == null || verifyTemplateCode.isBlank()) {
            log.warn("验证码模板 ID (aliyun.sms.verify-template-code) 未配置，跳过发送");
            return false;
        }

        try {
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phoneNumber)
                    .setSignName(signName)
                    .setTemplateCode(verifyTemplateCode)
                    .setTemplateParam("{\"code\":\"" + code + "\"}");

            SendSmsResponse response = getClient().sendSms(request);
            String respCode = response.getBody().getCode();
            String respMsg = response.getBody().getMessage();

            if ("OK".equals(respCode)) {
                log.info("验证码短信发送成功 -> {}", phoneNumber);
                return true;
            } else {
                log.error("验证码短信发送失败 [{}]: code={}, msg={}", phoneNumber, respCode, respMsg);
                return false;
            }
        } catch (Exception e) {
            log.error("验证码短信发送异常 [{}]: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送真实的扣费核销短信给弱网机手
     */
    public boolean sendConfirmationSms(String phoneNumber, String orderCode) {
        if (!isConfigured()) {
            log.warn("AKSK 未配置，跳过核销短信发送 -> {}", phoneNumber);
            return false;
        }
        if (templateCode == null || templateCode.isBlank()) {
            log.warn("核销模板 ID (aliyun.sms.template-code) 未配置，跳过发送");
            return false;
        }

        try {
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phoneNumber)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    // 传入 JSON 变量供短信模板渲染: "您的订单 ${code} 已核销"
                    .setTemplateParam("{\"code\":\"" + orderCode + "\"}");

            SendSmsResponse response = getClient().sendSms(request);
            String respCode = response.getBody().getCode();
            String respMsg = response.getBody().getMessage();

            if ("OK".equals(respCode)) {
                log.info("核销短信发送成功 -> {} 订单={}", phoneNumber, orderCode);
                return true;
            } else {
                log.error("核销短信发送失败 [{}]: code={}, msg={}", phoneNumber, respCode, respMsg);
                return false;
            }
        } catch (Exception e) {
            log.error("核销短信发送异常 [{}]: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }
}
