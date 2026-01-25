package com.vv.cloudfarming.order.mq.constant;

import java.util.List;

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

    // 投递消息的时间列表
    List<Integer> timeList = List.of(30000, 60000, 90000, 120000, 150000, 180000, 270000);
}
