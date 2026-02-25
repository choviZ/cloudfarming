package com.vv.cloudfarming.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderPageReqDTO;
import com.vv.cloudfarming.order.dto.resp.*;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     */
    OrderCreateRespDTO createOrderV2(Long userId, OrderCreateReqDTO requestParam);

    /**
     * 分页查询订单，部分订单信息聚合简要的商品信息（用户使用）
     */
    IPage<OrderPageWithProductInfoRespDTO> listOrderWithProductInfo(OrderPageReqDTO requestParam);

    /**
     * 分页查询订单，仅全面的订单信息（管理员使用）
     */
    IPage<OrderPageRespDTO> listOrders(OrderPageReqDTO requestParam);

    /**
     * 获取认养订单详情
     * @param orderNo 订单号
     */
    List<AdoptOrderDetailRespDTO> getAdoptOrderDetail(String orderNo);

    /**
     * 获取普通商品订单详情
     * @param orderNo 订单号
     */
    List<SkuOrderDetailRespDTO> getSkuOrderDetail(String orderNo);
}
