package com.vv.cloudfarming.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderPageReqDTO;
import com.vv.cloudfarming.order.dto.resp.OrderCreateRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderPageRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderPageWithProductInfoRespDTO;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     */
    OrderCreateRespDTO createOrder(Long userId, OrderCreateReqDTO reqDTO);

    /**
     * 分页查询订单，部分订单信息聚合简要的商品信息（用户使用）
     */
    IPage<OrderPageWithProductInfoRespDTO> listOrderWithProductInfo(OrderPageReqDTO requestParam);

    /**
     * 分页查询订单，仅全面的订单信息（管理员使用）
     */
    IPage<OrderPageRespDTO> listOrders(OrderPageReqDTO requestParam);
}
