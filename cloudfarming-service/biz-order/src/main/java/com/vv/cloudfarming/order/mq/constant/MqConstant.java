package com.vv.cloudfarming.order.mq.constant;

/**
 * 常量定义
 */
public interface MqConstant {

    // 交换机
    String DELAY_EXCHANGE = "trade.delay.topic";

    // 延迟订单队列
    String DELAY_ORDER_QUEUE = "trade.order.delay.queue";

    // 延迟订单routing key
    String DELAY_ORDER_ROUTING_KEY = "order.query";
}
