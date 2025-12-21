package com.vv.cloudfarming.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;

/**
 * 订单服务接口层
 */
public interface OrderService extends IService<OrderDO> {

    /**
     * 创建订单
     *
     * @param requestParam 参数
     */
    void createOrder(OrderCreateReqDTO requestParam);
}
