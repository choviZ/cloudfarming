package com.vv.cloudfarming.product.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车响应对象
 */
@Data
public class CartRespDTO implements Serializable {

    /**
     * 购物车项列表
     */
    private List<CartItemRespDTO> cartItems;

    /**
     * 选中商品总数量
     */
    private Integer totalQuantity;

    /**
     * 选中商品总金额
     */
    private BigDecimal totalAmount;

    /**
     * 选中商品是否有库存
     */
    private Boolean allHasStock;
}