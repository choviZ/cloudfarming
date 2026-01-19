package com.vv.cloudfarming.order.mq.listener;

import com.vv.cloudfarming.order.enums.OrderStatusEnum;
import com.vv.cloudfarming.order.enums.PayStatusEnum;
import com.vv.cloudfarming.order.mq.DelayMessageProcessor;
import com.vv.cloudfarming.order.mq.constant.MqConstant;
import com.vv.cloudfarming.order.mq.modal.MultiDelayMessage;
import com.vv.cloudfarming.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 监听器：检查订单状态
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderStatusCheckListener {

    private final OrderService orderService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConstant.DELAY_ORDER_QUEUE, durable = "true"),
            exchange = @Exchange(value = MqConstant.DELAY_EXCHANGE, delayed = "true", type = ExchangeTypes.TOPIC),
            key = MqConstant.DELAY_ORDER_ROUTING_KEY
    ))
    public void listenOrderDelayMessage(MultiDelayMessage<Long> msg) {
        log.info("消费者收到延迟消息：{}", msg.toString());
        Long id = msg.getData();
        // 查询订单状态
        PayStatusEnum payStatusEnum = orderService.queryOrderPayStatusById(id);
        // 是否已支付
        if (PayStatusEnum.PAID.equals(payStatusEnum)) {
            // 已支付-修改订单状态
            orderService.updateOrderStatus(id, OrderStatusEnum.PENDING_SHIPMENT);
            log.info("{}已支付，订单状态已修改", id);
        } else {
            // 未支付-判断是否存在延迟时间
            if (msg.hasNextDelay()) {
                // 存在-继续投递
                int delay = msg.removeNextDelay().intValue();
                rabbitTemplate.convertAndSend(
                        MqConstant.DELAY_EXCHANGE,
                        MqConstant.DELAY_ORDER_ROUTING_KEY,
                        msg,
                        new DelayMessageProcessor(delay)
                );
                log.info("{}未支付，发送延迟消息，延迟时间：{}秒", id, delay / 1000);
            } else {
                // 不存在-结束订单
                orderService.updateOrderStatus(id, OrderStatusEnum.CANCEL);
                // TODO 释放库存
                log.info("{}订单已结束，释放库存", id);
            }
        }
    }
}
