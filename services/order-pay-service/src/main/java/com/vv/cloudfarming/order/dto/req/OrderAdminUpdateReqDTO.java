package com.vv.cloudfarming.order.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员更新订单请求参数
 */
@Data
public class OrderAdminUpdateReqDTO {

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 物流单号
     */
    private String logisticsNo;

    /**
     * 物流公司
     */
    private String logisticsCompany;

    /**
     * 收货人姓名
     */
    private String receiveName;

    /**
     * 收货人手机号
     */
    private String receivePhone;
}
