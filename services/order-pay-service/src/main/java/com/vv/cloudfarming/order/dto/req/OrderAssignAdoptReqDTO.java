package com.vv.cloudfarming.order.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 农户分配认养牲畜请求参数
 */
@Data
public class OrderAssignAdoptReqDTO {

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 分配明细
     */
    @Valid
    @NotEmpty(message = "分配明细不能为空")
    private List<OrderAssignAdoptItemReqDTO> items;
}
