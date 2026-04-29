package com.nongzhushou.common.mq;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * MQ 消息生产者：封装发送各类业务消息的方法。
 */
@Component
public class MatchMessageProducer {

    private static final Logger log = LoggerFactory.getLogger(MatchMessageProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public MatchMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送匹配派单消息
     */
    public void sendMatchDispatch(Long demandId) {
        Map<String, Object> message = Map.of(
                "type", "MATCH_DISPATCH",
                "demandId", demandId,
                "timestamp", System.currentTimeMillis()
        );
        rabbitTemplate.convertAndSend(
                RabbitConfig.MATCH_EXCHANGE,
                RabbitConfig.MATCH_ROUTING_KEY,
                message
        );
        log.info("[MQ] 发送匹配派单消息 demandId={}", demandId);
    }

    /**
     * 发送订单状态变更消息（预留对接短信/推送）
     */
    public void sendOrderStatusChange(Long orderId, String fromStatus, String toStatus) {
        Map<String, Object> message = Map.of(
                "type", "ORDER_STATUS_CHANGE",
                "orderId", orderId,
                "fromStatus", fromStatus,
                "toStatus", toStatus,
                "timestamp", System.currentTimeMillis()
        );
        rabbitTemplate.convertAndSend(
                RabbitConfig.ORDER_EXCHANGE,
                RabbitConfig.ORDER_ROUTING_KEY,
                message
        );
        log.info("[MQ] 发送订单状态变更消息 orderId={} {} → {}", orderId, fromStatus, toStatus);
    }

    /**
     * 发送超时处理结果消息
     */
    public void sendTimeoutResult(Long attemptId, String timeoutType) {
        Map<String, Object> message = Map.of(
                "type", "TIMEOUT_RESULT",
                "attemptId", attemptId,
                "timeoutType", timeoutType,
                "timestamp", System.currentTimeMillis()
        );
        rabbitTemplate.convertAndSend(
                RabbitConfig.TIMEOUT_EXCHANGE,
                RabbitConfig.TIMEOUT_ROUTING_KEY,
                message
        );
        log.info("[MQ] 发送超时处理结果 attemptId={} type={}", attemptId, timeoutType);
    }
}
