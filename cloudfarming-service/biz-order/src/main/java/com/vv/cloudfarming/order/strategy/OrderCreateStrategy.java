package com.vv.cloudfarming.order.strategy;

import cn.hutool.json.JSONObject;
import com.vv.cloudfarming.order.dao.entity.OrderDO;

import java.util.List;

/**
 * 订单创建策略接口
 */
public interface OrderCreateStrategy {

    /**
     * 创建业务订单
     * @param userId 用户ID
     * @param payOrderNo 支付单号 (透传给生成的订单)
     * @param bizData 业务数据
     * @return 生成的订单列表
     */
    List<OrderDO> createOrders(Long userId, String payOrderNo, JSONObject bizData);
}
