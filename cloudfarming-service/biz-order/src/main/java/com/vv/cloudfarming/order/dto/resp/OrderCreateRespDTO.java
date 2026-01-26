package com.vv.cloudfarming.order.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建订单响应对象
 */
@Data
public class OrderCreateRespDTO {

    // 需要支付的金额
    private BigDecimal payAmount;

    // 订单过期时间（前端做倒计时用）
    private Long expireTime;

    // 支付单号
    private String payOrderNo;
}
