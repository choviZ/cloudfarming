package com.vv.cloudfarming.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.order.dto.req.OrderAssignAdoptReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderAdminUpdateReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderFulfillAdoptReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderPageReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderReceiveReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderShipReqDTO;
import com.vv.cloudfarming.order.dto.req.SeckillCreateReqDTO;
import com.vv.cloudfarming.order.dto.resp.AdoptOrderDetailRespDTO;
import com.vv.cloudfarming.order.dto.resp.FarmerOrderStatisticsRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderCreateRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderPageRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderPageWithProductInfoRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderSimpleRespDTO;
import com.vv.cloudfarming.order.dto.resp.SkuOrderDetailRespDTO;

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
     * 秒杀下单
     */
    String createSeckillOrder(SeckillCreateReqDTO requestParam);

    /**
     * 分页查询订单，部分订单信息聚合简要的商品信息（用户使用）
     */
    IPage<OrderPageWithProductInfoRespDTO> listOrderWithProductInfo(OrderPageReqDTO requestParam);

    /**
     * 分页查询订单，仅全面的订单信息（管理员使用）
     */
    IPage<OrderPageRespDTO> listOrders(OrderPageReqDTO requestParam);

    /**
     * 分页查询当前农户店铺订单
     */
    IPage<OrderPageRespDTO> listCurrentFarmerOrders(OrderPageReqDTO requestParam);

    /**
     * 管理员更新订单
     */
    void updateOrderByAdmin(OrderAdminUpdateReqDTO requestParam);

    /**
     * 当前农户发货
     */
    void shipCurrentFarmerOrder(OrderShipReqDTO requestParam);

    /**
     * 当前用户确认收货
     */
    void receiveCurrentUserOrder(OrderReceiveReqDTO requestParam);

    /**
     * 当前农户分配认养牲畜
     */
    void assignCurrentFarmerAdoptOrder(OrderAssignAdoptReqDTO requestParam);

    /**
     * 当前农户完成认养履约
     */
    void fulfillCurrentFarmerAdoptInstance(OrderFulfillAdoptReqDTO requestParam);

    /**
     * 获取认养订单详情
     *
     * @param orderNo 订单号
     */
    List<AdoptOrderDetailRespDTO> getAdoptOrderDetail(String orderNo);

    /**
     * 获取普通商品订单详情
     *
     * @param orderNo 订单号
     */
    List<SkuOrderDetailRespDTO> getSkuOrderDetail(String orderNo);

    /**
     * 获取农户订单统计
     */
    FarmerOrderStatisticsRespDTO getFarmerOrderStatistics();

    /**
     * 按订单ID批量查询订单简要信息
     */
    List<OrderSimpleRespDTO> listSimpleOrdersByIds(List<Long> orderIds);
}
