package com.vv.cloudfarming.order.dto.req;

import com.vv.cloudfarming.order.dto.common.ProductItemDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建订单请求参数
 */
@Data
public class OrderCreateReqDTO {

    /**
     * 订单类型
     */
    @NotNull
    private Integer orderType;

    /**
     * 收获地址id
     */
    @NotNull
    private Long receiveId;

    /**
     * 下单的商品
     */
    private List<ProductItemDTO> items;
}
