package com.vv.cloudfarming.order.dto.req;

import com.vv.cloudfarming.order.dto.OrderItemDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建订单的请求入参DTO
 */
@Data
public class OrderCreateReqDTO {

    /**
     * 商品列表
     */
    @NotEmpty(message = "商品列表不能为空")
    @Valid
    private List<OrderItemDTO> items;

    /**
     * 收货相关信息
     */
    @NotNull(message = "收货信息不能为空")
    private Long receiveId;

    /**
     * 订单备注
     */
    private String remark;
}