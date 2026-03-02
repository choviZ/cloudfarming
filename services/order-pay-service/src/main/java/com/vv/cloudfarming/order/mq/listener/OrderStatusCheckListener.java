package com.vv.cloudfarming.order.mq.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailAdoptDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailSkuDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailAdoptMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailSkuMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.enums.OrderStatusEnum;
import com.vv.cloudfarming.order.enums.PayStatusEnum;
import com.vv.cloudfarming.order.mq.DelayMessageProcessor;
import com.vv.cloudfarming.order.mq.constant.MqConstant;
import com.vv.cloudfarming.order.mq.modal.MultiDelayMessage;
import com.vv.cloudfarming.order.remote.AdoptItemRemoteService;
import com.vv.cloudfarming.order.remote.SkuRemoteService;
import com.vv.cloudfarming.product.dto.req.LockStockReqDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 监听器：检查订单状态
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderStatusCheckListener {

    private final OrderMapper orderMapper;
    private final AdoptItemRemoteService adoptItemRemoteService;
    private final RabbitTemplate rabbitTemplate;
    private final OrderDetailAdoptMapper orderDetailAdoptMapper;
    private final OrderDetailSkuMapper orderDetailSkuMapper;
    private final SkuRemoteService skuRemoteService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConstant.DELAY_ORDER_QUEUE, durable = "true"),
            exchange = @Exchange(value = MqConstant.DELAY_EXCHANGE, delayed = "true", type = ExchangeTypes.TOPIC),
            key = MqConstant.DELAY_ORDER_ROUTING_KEY
    ))
    public void listenOrderDelayMessage(MultiDelayMessage<String> msg) {
        log.info("消费者收到延迟消息：{}", msg.toString());
        String orderNo = msg.getData();
        OrderDO order = orderMapper.selectOne(Wrappers.lambdaQuery(OrderDO.class).eq(OrderDO::getOrderNo, orderNo));
        if (order == null) {
            log.error("订单不存在，订单号：{}", orderNo);
            return;
        }
        if (PayStatusEnum.UNPAID.getCode().equals(order.getOrderStatus())) {
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
                log.info("订单：{}未支付，发送延迟消息，延迟时间：{}秒", orderNo, delay / 1000);
            } else {
                // 不存在-结束订单
                LambdaUpdateWrapper<OrderDO> wrapper = Wrappers.lambdaUpdate(OrderDO.class)
                        .set(OrderDO::getOrderStatus, OrderStatusEnum.CANCEL.getCode())
                        .eq(OrderDO::getOrderNo, orderNo)
                        .eq(OrderDO::getOrderStatus, OrderStatusEnum.PENDING_PAYMENT.getCode());
                int updated = orderMapper.update(wrapper);
                if (SqlHelper.retBool(updated)) {
                    log.info("订单取消成功，订单号：{}", orderNo);
                } else {
                    log.info("订单取消失败，状态已变更，订单号：{}", orderNo);
                    return;
                }

                // 释放锁定的库存
                try {
                    switch (order.getOrderType()) {
                        case 0:
                            // 查订单详情
                            List<OrderDetailAdoptDO> adoptDetails = orderDetailAdoptMapper.selectList(
                                    Wrappers.lambdaQuery(OrderDetailAdoptDO.class).eq(OrderDetailAdoptDO::getOrderNo, order.getOrderNo())
                            );
                            for (OrderDetailAdoptDO detail : adoptDetails) {
                                LockStockReqDTO lockStockReqDTO = new LockStockReqDTO();
                                lockStockReqDTO.setId(detail.getAdoptItemId());
                                lockStockReqDTO.setQuantity(detail.getQuantity());
                                adoptItemRemoteService.unlockAdoptItemStock(lockStockReqDTO);
                            }
                            break;
                        case 1:
                            List<OrderDetailSkuDO> skuDetails = orderDetailSkuMapper.selectList(
                                    Wrappers.lambdaQuery(OrderDetailSkuDO.class).eq(OrderDetailSkuDO::getOrderNo, order.getOrderNo())
                            );
                            for (OrderDetailSkuDO skuDetail : skuDetails) {
                                LockStockReqDTO lockStockReqDTO = new LockStockReqDTO();
                                lockStockReqDTO.setId(skuDetail.getSkuId());
                                lockStockReqDTO.setQuantity(skuDetail.getQuantity());
                                skuRemoteService.unlockStock(lockStockReqDTO);
                            }
                            break;
                    }
                    log.info(":{}订单已结束，释放锁定的库存", orderNo);
                } catch (Exception e) {
                    log.error("释放库存失败，订单号：{}", orderNo);
                }
            }
        }
    }
}
