package com.vv.cloudfarming.order.strategy;

import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单策略工厂
 */
@Component("refactorOrderStrategyFactory")
@RequiredArgsConstructor
public class OrderStrategyFactory {

    private final Map<Integer, OrderCreateStrategy> strategyMap = new ConcurrentHashMap<>();
    
    // 构造注入所有策略实现
    private final GoodsOrderStrategy goodsOrderStrategy;
    private final AdoptOrderStrategy adoptOrderStrategy;

    public OrderCreateStrategy getStrategy(Integer orderType) {
        if (orderType == null) {
            throw new ServiceException("订单类型不能为空");
        }
        if (orderType == OrderTypeConstant.GOODS) {
            return goodsOrderStrategy;
        } else if (orderType == OrderTypeConstant.ADOPT) {
            return adoptOrderStrategy;
        }
        throw new ServiceException("未知的订单类型: " + orderType);
    }
}
