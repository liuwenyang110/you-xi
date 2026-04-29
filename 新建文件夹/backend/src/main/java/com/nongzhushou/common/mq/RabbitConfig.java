package com.nongzhushou.common.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置中心。
 * 定义三组 Exchange → Queue → Binding：
 *   1. 匹配派单（match）
 *   2. 订单状态变更（order）
 *   3. 超时扫描结果（timeout）
 */
@Configuration
public class RabbitConfig {

    // ============ 匹配派单 ============
    public static final String MATCH_EXCHANGE = "match.exchange";
    public static final String MATCH_DISPATCH_QUEUE = "match.dispatch.queue";
    public static final String MATCH_ROUTING_KEY = "match.dispatch";

    // ============ 订单状态 ============
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_STATUS_QUEUE = "order.status.queue";
    public static final String ORDER_ROUTING_KEY = "order.status";

    // ============ 超时处理 ============
    public static final String TIMEOUT_EXCHANGE = "timeout.exchange";
    public static final String TIMEOUT_RESULT_QUEUE = "timeout.result.queue";
    public static final String TIMEOUT_ROUTING_KEY = "timeout.result";

    // ============ 死信队列（失败兜底） ============
    public static final String DLX_EXCHANGE = "dlx.exchange";
    public static final String DLX_QUEUE = "dlx.queue";
    public static final String DLX_ROUTING_KEY = "dlx.routing";

    // --- JSON 序列化 ---
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // --- 匹配 ---
    @Bean
    public DirectExchange matchExchange() {
        return new DirectExchange(MATCH_EXCHANGE);
    }

    @Bean
    public Queue matchDispatchQueue() {
        return QueueBuilder.durable(MATCH_DISPATCH_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding matchBinding() {
        return BindingBuilder.bind(matchDispatchQueue()).to(matchExchange()).with(MATCH_ROUTING_KEY);
    }

    // --- 订单 ---
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue orderStatusQueue() {
        return QueueBuilder.durable(ORDER_STATUS_QUEUE).build();
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderStatusQueue()).to(orderExchange()).with(ORDER_ROUTING_KEY);
    }

    // --- 超时 ---
    @Bean
    public DirectExchange timeoutExchange() {
        return new DirectExchange(TIMEOUT_EXCHANGE);
    }

    @Bean
    public Queue timeoutResultQueue() {
        return QueueBuilder.durable(TIMEOUT_RESULT_QUEUE).build();
    }

    @Bean
    public Binding timeoutBinding() {
        return BindingBuilder.bind(timeoutResultQueue()).to(timeoutExchange()).with(TIMEOUT_ROUTING_KEY);
    }

    // --- 死信 ---
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue dlxQueue() {
        return QueueBuilder.durable(DLX_QUEUE).build();
    }

    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(DLX_ROUTING_KEY);
    }
}
