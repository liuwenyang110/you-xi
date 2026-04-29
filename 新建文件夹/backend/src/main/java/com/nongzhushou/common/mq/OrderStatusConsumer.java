package com.nongzhushou.common.mq;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单状态变更消费者。
 * 当前：记录日志。
 * 未来扩展：发短信通知、微信模板消息推送、数据统计更新。
 */
@Component
public class OrderStatusConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusConsumer.class);

    @RabbitListener(queues = RabbitConfig.ORDER_STATUS_QUEUE)
    public void handleOrderStatusChange(Map<String, Object> message) {
        Long orderId = toLong(message.get("orderId"));
        String fromStatus = (String) message.get("fromStatus");
        String toStatus = (String) message.get("toStatus");

        log.info("[MQ] 订单状态变更 orderId={} {} → {}", orderId, fromStatus, toStatus);

        // TODO: 扩展点
        // 1. 短信通知农户/机主
        // 2. 微信模板消息推送
        // 3. 统计数据更新
        // 4. 审计日志记录
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
