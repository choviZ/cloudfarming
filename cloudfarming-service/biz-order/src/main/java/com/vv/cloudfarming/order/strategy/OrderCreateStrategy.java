package com.vv.cloudfarming.order.strategy;

import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.resp.OrderCreateRespDTO;

/**
 * 创建订单策略接口
 */
public interface OrderCreateStrategy {

    /**
     * 获取订单类型
     */
    Integer getOrderType();

    /**
     * 创建订单
     */
    OrderCreateRespDTO create(Long userId, OrderCreateReqDTO reqDTO);
}
