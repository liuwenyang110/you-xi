package com.nongzhushou.common.mq;

import com.nongzhushou.matchflow.service.MatchFlowService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 匹配消息消费者：监听匹配队列，异步执行派单逻辑。
 * 消费失败 3 次后消息自动进入死信队列。
 */
@Component
public class MatchMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(MatchMessageConsumer.class);

    private final MatchFlowService matchFlowService;

    public MatchMessageConsumer(MatchFlowService matchFlowService) {
        this.matchFlowService = matchFlowService;
    }

    @RabbitListener(queues = RabbitConfig.MATCH_DISPATCH_QUEUE, concurrency = "2-5")
    public void handleMatchDispatch(Map<String, Object> message) {
        Long demandId = toLong(message.get("demandId"));
        if (demandId == null) {
            log.warn("[MQ] 匹配消息缺少 demandId，丢弃: {}", message);
            return;
        }
        log.info("[MQ] 收到匹配派单消息 demandId={}", demandId);
        try {
            Long attemptId = matchFlowService.dispatchNextAttempt(demandId);
            log.info("[MQ] 匹配派单完成 demandId={} attemptId={}", demandId, attemptId);
        } catch (Exception e) {
            log.error("[MQ] 匹配派单失败 demandId={}", demandId, e);
            throw e; // 抛出异常触发重试，3 次后进死信队列
        }
    }

    @RabbitListener(queues = RabbitConfig.TIMEOUT_RESULT_QUEUE)
    public void handleTimeoutResult(Map<String, Object> message) {
        Long attemptId = toLong(message.get("attemptId"));
        String timeoutType = (String) message.get("timeoutType");
        if (attemptId == null) {
            log.warn("[MQ] 超时消息缺少 attemptId，丢弃: {}", message);
            return;
        }
        log.info("[MQ] 收到超时处理结果 attemptId={} type={}", attemptId, timeoutType);
        try {
            if ("OWNER_TIMEOUT".equals(timeoutType)) {
                matchFlowService.ownerTimeout(attemptId);
            }
            // 其他超时类型可在此扩展
        } catch (Exception e) {
            log.error("[MQ] 超时处理失败 attemptId={}", attemptId, e);
        }
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
