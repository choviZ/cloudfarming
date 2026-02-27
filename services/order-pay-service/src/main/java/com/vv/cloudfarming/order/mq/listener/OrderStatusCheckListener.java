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
    public void listenOrderDelayMessage(MultiDelayMessage<Long> msg) {
        log.info("消费者收到延迟消息：{}", msg.toString());
        Long id = msg.getData();
        // 查询订单状态
        OrderDO order = orderMapper.selectById(id);
        if (order == null) {
            throw new ServiceException("订单不存在：" + id);
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
                log.info("订单：{}未支付，发送延迟消息，延迟时间：{}秒", id, delay / 1000);
            } else {
                // 不存在-结束订单
                LambdaUpdateWrapper<OrderDO> wrapper = Wrappers.lambdaUpdate(OrderDO.class)
                        .set(OrderDO::getOrderStatus, OrderStatusEnum.CANCEL.getCode())
                        .eq(OrderDO::getId, id)
                        .eq(OrderDO::getOrderStatus, OrderStatusEnum.PENDING_PAYMENT.getCode());
                int updated = orderMapper.update(wrapper);
                if (SqlHelper.retBool(updated)) {
                    log.info("订单取消成功，ID：{}", id);
                } else {
                    log.info("订单取消失败，状态已变更，ID：{}", id);
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
                    log.info(":{}订单已结束，释放锁定的库存", id);
                } catch (Exception e) {
                    log.error("释放库存失败，订单ID：{}", id);
                    throw new ServiceException("库存释放失败，请重试");
                }
            }
        }
    }
}
