package com.vv.cloudfarming.order.service;

import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.resp.OrderCreateRespDTO;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     */
    OrderCreateRespDTO createOrder(Long userId, OrderCreateReqDTO reqDTO);
}
