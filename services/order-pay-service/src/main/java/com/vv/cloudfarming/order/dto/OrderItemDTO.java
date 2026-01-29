package com.vv.cloudfarming.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单项
 */
@Data
public class OrderItemDTO {

    /**
     * 商品SKU ID
     */
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    /**
     * 商品数量
     */
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;
}
