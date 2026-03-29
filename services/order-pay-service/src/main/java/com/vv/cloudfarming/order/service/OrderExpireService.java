package com.vv.cloudfarming.order.service;

/**
 * 订单到期关闭服务
 */
public interface OrderExpireService {

    boolean closeExpiredPayOrder(String payOrderNo);

    boolean closeExpiredPayOrderByOrderNo(String orderNo);

    int closeExpiredPayOrders(int limit);
}
