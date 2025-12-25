package com.vv.cloudfarming.cart.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 购物车项实体类（用于Redis存储）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDO implements Serializable {

    /**
     * 商品SKU ID
     */
    private Long skuId;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 是否选中
     */
    private Boolean selected;

    /**
     * 是否有库存
     */
    private Boolean hasStock;
}