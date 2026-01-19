package com.vv.cloudfarming.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.resp.OrderInfoRespDTO;
import com.vv.cloudfarming.order.enums.OrderStatusEnum;
import com.vv.cloudfarming.order.enums.PayStatusEnum;

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

    /**
     * 根据id查询订单信息
     * @param id
     */
    OrderInfoRespDTO queryOrderById(Long id);

    /**
     * 根据订单id查询订单支付状态
     * @param id 订单id
     * @return 支付状态枚举
     */
    PayStatusEnum queryOrderPayStatusById(Long id);

    /**
     * 修改订单支付状态
     * @param id
     * @param payStatusEnum
     */
    void updateOrderPayStatus(Long id, PayStatusEnum payStatusEnum);

    /**
     * 根据订单id查询订单状态
     * @param id 订单id
     * @return 订单状态枚举
     */
    OrderStatusEnum queryOrderStatusById(Long id);

    /**
     * 修改订单状态
     * @param id 订单id
     * @param orderStatusEnum 订单状态枚举
     */
    void updateOrderStatus(Long id, OrderStatusEnum orderStatusEnum);
}
