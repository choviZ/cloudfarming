package com.vv.cloudfarming.order.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户确认收货请求参数
 */
@Data
public class OrderReceiveReqDTO {

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;
}
