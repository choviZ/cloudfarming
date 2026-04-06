package com.vv.cloudfarming.order.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 农户发货请求参数
 */
@Data
public class OrderShipReqDTO {

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 物流公司
     */
    @NotBlank(message = "物流公司不能为空")
    private String logisticsCompany;

    /**
     * 物流单号
     */
    @NotBlank(message = "物流单号不能为空")
    private String logisticsNo;
}
