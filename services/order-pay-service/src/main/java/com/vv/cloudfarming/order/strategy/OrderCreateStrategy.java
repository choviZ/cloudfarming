package com.vv.cloudfarming.order.strategy;

import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dto.common.ProductItemDTO;
import com.vv.cloudfarming.order.service.basics.chain.OrderContext;

import java.math.BigDecimal;
import java.util.List;

public interface OrderCreateStrategy {

    /**
     * 该策略支持的业务类型
     */
    Integer bizType();

    /**
     * 根据 ItemDTO 获取 shopId（拆单用）
     */
    Long resolveShopId(ProductItemDTO item, OrderContext ctx);

    /**
     * 计算某个 shop 下的订单总金额
     */
    BigDecimal calculateOrderAmount(List<ProductItemDTO> items, OrderContext ctx);

    /**
     * 构建订单明细（落库）
     */
    void createOrderDetails(OrderDO order, List<ProductItemDTO> items, OrderContext ctx);

    /**
     * 锁定库存
     */
    void lockedStock(List<ProductItemDTO> items);

    /**
     * 释放已锁定库存，并恢复真实库存（用于下单流程失败补偿）
     */
    void unlockStock(List<ProductItemDTO> items);
}
