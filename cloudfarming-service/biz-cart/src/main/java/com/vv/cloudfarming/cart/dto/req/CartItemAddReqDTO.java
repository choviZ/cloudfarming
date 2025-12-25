package com.vv.cloudfarming.cart.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 添加购物车项参数对象
 */
@Data
public class CartItemAddReqDTO implements Serializable {

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
    private Boolean selected = true;
}