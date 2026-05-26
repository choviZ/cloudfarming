package com.vv.cloudfarming.order.service;

/**
 * 订单到期关闭与取消服务
 */
public interface OrderExpireService {

    /**
     * 关闭指定的过期未支付支付单，同时取消关联订单并释放锁定库存
     *
     * @param payOrderNo 支付单号
     * @return 是否成功关闭
     */
    boolean closeExpiredPayOrder(String payOrderNo);

    /**
     * 根据订单号关闭其所属的过期未支付支付单
     *
     * @param orderNo 订单号
     * @return 是否成功关闭
     */
    boolean closeExpiredPayOrderByOrderNo(String orderNo);

    /**
     * 批量关闭过期未支付的支付单（定时补偿任务调用）
     *
     * @param limit 每批处理上限
     * @return 本次关闭的支付单数量
     */
    int closeExpiredPayOrders(int limit);

    /**
     * 用户主动取消未支付的支付单，校验归属后关闭支付单、取消关联订单并释放锁定库存
     *
     * @param payOrderNo 支付单号
     * @param userId     当前登录用户ID，用于校验支付单归属
     * @return 是否成功取消
     */
    boolean cancelPayOrder(String payOrderNo, Long userId);
}
