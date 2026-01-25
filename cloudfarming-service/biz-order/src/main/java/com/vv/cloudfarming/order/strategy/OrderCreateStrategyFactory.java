package com.vv.cloudfarming.order.strategy;

import com.vv.cloudfarming.common.exception.ClientException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 获取策略实现类的工厂
 */
@Component
public class OrderCreateStrategyFactory {

    /**
     * 存储所有策略
     */
    private final Map<Integer, OrderCreateStrategy> strategyMap;

    /**
     * 注册策略
     */
    public OrderCreateStrategyFactory(List<OrderCreateStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(OrderCreateStrategy::getOrderType, Function.identity()));
    }

    /**
     * 获取策略
     */
    public OrderCreateStrategy getStrategy(Integer orderType) {
        OrderCreateStrategy strategy = strategyMap.get(orderType);
        if (strategy == null) {
            throw new ClientException("不支持的订单类型");
        }
        return strategy;
    }
}
