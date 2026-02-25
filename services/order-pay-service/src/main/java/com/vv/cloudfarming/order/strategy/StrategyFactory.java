package com.vv.cloudfarming.order.strategy;

import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StrategyFactory {

    private final AdoptOrderCreateStrategy adoptOrderCreateStrategy;
    private final SkuOrderCreateStrategy skuOrderCreateStrategy;

    public OrderCreateStrategy get(Integer bizType){
        if (OrderTypeConstant.ADOPT == bizType){
            return adoptOrderCreateStrategy;
        }else if (OrderTypeConstant.GOODS == bizType){
            return skuOrderCreateStrategy;
        }
        return null;
    }
}
