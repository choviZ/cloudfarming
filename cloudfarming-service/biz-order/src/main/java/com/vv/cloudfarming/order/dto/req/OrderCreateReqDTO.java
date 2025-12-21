package com.vv.cloudfarming.order.dto.req;

import com.vv.cloudfarming.order.dto.ReceiveInfoDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单的请求入参DTO
 */
@Data
public class OrderCreateReqDTO {

    /**
     * 商品ID
     */
    @NotNull
    private Long productId;

    /**
     * 商品数量
     */
    @Min(1)
    private Integer quantity;

    /**
     * 收货相关信息
     */
    private ReceiveInfoDTO receiveInfo;

    /**
     * 订单备注
     */
    private String remark;
}