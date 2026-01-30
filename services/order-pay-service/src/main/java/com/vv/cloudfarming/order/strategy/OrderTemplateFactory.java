package com.vv.cloudfarming.order.strategy;

import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 订单模板工厂
 * 根据订单类型返回对应的订单创建模板实现
 */
@Component
@RequiredArgsConstructor
public class OrderTemplateFactory {

    private final GoodsOrderTemplate goodsOrderTemplate;
    private final AdoptOrderTemplate adoptOrderTemplate;

    /**
     * 根据订单类型获取对应的模板
     *
     * @param orderType 订单类型
     * @return 订单创建模板
     */
    public AbstractOrderCreateTemplate<?, ?> getTemplate(Integer orderType) {
        if (orderType == null) {
            throw new ServiceException("订单类型不能为空");
        }
        return switch (orderType) {
            case OrderTypeConstant.GOODS -> goodsOrderTemplate;
            case OrderTypeConstant.ADOPT -> adoptOrderTemplate;
            default -> throw new ServiceException("未知的订单类型: " + orderType);
        };
    }
}
