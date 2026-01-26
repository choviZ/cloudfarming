package com.vv.cloudfarming.order.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建订单响应对象
 */
@Data
public class OrderCreateRespDTO {

    // 核心标识
    private Long orderId;        // 数据库主键（用于后端查询）
    private String orderNo;      // 业务订单号（用于展示和支付）

    // 支付相关（这是重点！）
    private BigDecimal payAmount; // 需要支付的金额
    private Long expireTime;      // 订单过期时间（前端做倒计时用）
    
    private Long payOrderId;
    private String payOrderNo;
}
