package com.vv.cloudfarming.order.strategy;

import cn.hutool.json.JSONObject;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.resp.OrderCreateRespDTO;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.mq.DelayMessageProcessor;
import com.vv.cloudfarming.order.mq.constant.MqConstant;
import com.vv.cloudfarming.order.mq.modal.MultiDelayMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 创建订单模板
 */
@Slf4j
public abstract class AbstractOrderCreateTemplate<T> {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Transactional
    public OrderCreateRespDTO create(Long userId, OrderCreateReqDTO reqDTO) {
        // 解析数据
        T bizData = parseBizData(reqDTO.getBizData());
        // 参数 校验
        validate(userId, bizData);
        OrderDO order = null;
        boolean stockLocked = false;
        try {
            lockStock(userId, bizData);
            stockLocked = true;
            order = buildMainOrder(userId, bizData, reqDTO.getOrderType());
            saveMainOrder(order);
            Long subOrderId = createSubOrder(userId, order, bizData);
            afterCreate(userId, order, bizData, subOrderId);
            // 发送消息
            MultiDelayMessage<Long> message = new MultiDelayMessage<>(order.getId(),MqConstant.timeList);
            rabbitTemplate.convertAndSend(
                    MqConstant.DELAY_EXCHANGE,MqConstant.DELAY_ORDER_ROUTING_KEY,message,new DelayMessageProcessor(message.removeNextDelay())
            );
            return buildResp(order.getId(), order.getOrderNo(), order.getTotalAmount(), order.getCreateTime());
        } catch (RuntimeException ex) {
            if (stockLocked) {
                try {
                    releaseStock(userId, bizData);
                } catch (RuntimeException ignored) {
                    log.error("库存释放失败：订单id{}",order.getId());
                }
            }
            throw ex;
        }
    }

    /**
     * 从json对象中解析业务数据
     */
    protected abstract T parseBizData(JSONObject bizData);

    /**
     * 校验数据合法性
     */
    protected abstract void validate(Long userId, T data);

    /**
     * 构建订单
     */
    protected abstract OrderDO buildMainOrder(Long userId, T data, Integer orderType);

    protected abstract Long createSubOrder(Long userId, OrderDO mainOrder, T data);

    protected abstract void lockStock(Long userId, T data);

    protected abstract void releaseStock(Long userId, T data);

    protected void afterCreate(Long userId, OrderDO mainOrder, T data, Long subOrderId) {
    }

    protected String generateOrderNo(Long userId) {
        long timestamp = System.currentTimeMillis() / 1000;
        String timePart = String.format("%08d", timestamp % 100000000);
        String userIdPart = String.format("%06d", userId);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String randomPart = String.format("%04d", random.nextInt(10000));
        return timePart + userIdPart + randomPart;
    }

    /**
     * 保存主订单
     */
    private void saveMainOrder(OrderDO mainOrder) {
        int inserted = orderMapper.insert(mainOrder);
        if (inserted != 1) {
            throw new ServiceException("主订单创建失败");
        }
    }

    /**
     * 构建响应
     */
    private OrderCreateRespDTO buildResp(Long orderId, String orderNo, BigDecimal payAmount, Date createTime) {
        OrderCreateRespDTO response = new OrderCreateRespDTO();
        response.setOrderId(orderId);
        response.setOrderNo(orderNo);
        response.setPayAmount(payAmount);
        response.setExpireTime(createTime.toInstant().plus(15, ChronoUnit.MINUTES).toEpochMilli());
        return response;
    }
}
