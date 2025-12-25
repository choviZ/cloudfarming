package com.vv.cloudfarming.order.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatusEnum {

    PENDING_PAYMENT(0, "未支付"),
    PENDING_SHIPMENT(1, "已支付（待发货）"),
    SHIPPED(2, "待收货"),
    COMPLETED(3, "已完成（确认收货）"),
    CANCEL(4, "已关闭（取消/退款）"),
    AFTER_SALE(5, "售后中");

    private final int code;
    private final String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}