package com.vv.cloudfarming.product.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新购物车项参数对象
 */
@Data
public class CartItemUpdateReqDTO implements Serializable {

    /**
     * 商品SKU ID
     */
    @NotNull(message = "商品SKU ID不能为空")
    private Long skuId;

    /**
     * 商品数量
     */
    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量至少为1")
    private Integer quantity;

    /**
     * 是否选中
     */
    @NotNull(message = "是否选中不能为空")
    private Boolean selected;
}