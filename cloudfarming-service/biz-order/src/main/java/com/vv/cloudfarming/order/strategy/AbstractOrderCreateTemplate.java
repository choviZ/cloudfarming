package com.vv.cloudfarming.order.strategy;

import cn.hutool.json.JSONObject;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.resp.OrderCreateRespDTO;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 创建订单模板
 */
public abstract class AbstractOrderCreateTemplate<T> {

    @Autowired
    protected OrderMapper orderMapper;

    public OrderCreateRespDTO create(Long userId, OrderCreateReqDTO reqDTO) {
        // 解析数据
        T bizData = parseBizData(reqDTO.getBizData());
        // 参数 校验
        validate(userId, bizData);
        // 构建主订单
        OrderDO mainOrder = buildMainOrder(userId, bizData, reqDTO.getOrderType());
        // 保存主订单
        saveMainOrder(mainOrder);
        // 创建子订单
        Long subOrderId = createSubOrder(userId, mainOrder, bizData);
        // 子订单创建后的处理
        afterCreate(userId, mainOrder, bizData, subOrderId);
        // 构建响应
        return buildResp(mainOrder.getId(), mainOrder.getOrderNo(), mainOrder.getTotalAmount(), mainOrder.getCreateTime());
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
