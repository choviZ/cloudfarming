package com.vv.cloudfarming.order.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 支付结果确认响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayConfirmRespDTO {

    /**
     * 支付单号
     */
    private String payOrderNo;

    /**
     * 是否已确认支付成功
     */
    private Boolean paid;

    /**
     * 当前支付状态
     */
    private Integer payStatus;

    /**
     * 当前交易状态
     */
    private String tradeStatus;

    /**
     * 支付金额
     */
    private BigDecimal totalAmount;

    /**
     * 提示信息
     */
    private String message;
}
