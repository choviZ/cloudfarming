package com.vv.cloudfarming.order.dto.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付单响应数据
 */
@Data
public class PayOrderRespDTO {

    /**
     * 支付单号
     */
    private String payOrderNo;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 支付状态
     */
    private Integer payStatus;

    /**
     * 业务状态
     */
    private Integer bizStatus;

    /**
     * 支付方式：alipay
     */
    private Integer payChannel;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 关联的商品信息
     */
    private List<PayOrderItemRespDTO> items;
}

