package com.vv.cloudfarming.order.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 取消支付单请求参数
 */
@Data
public class PayOrderCancelReqDTO {

    /**
     * 支付单号
     */
    @NotBlank(message = "支付单号不能为空")
    private String payOrderNo;
}
