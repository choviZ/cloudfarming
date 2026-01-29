package com.vv.cloudfarming.product.dto.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * 购物车项响应对象
 */
@Data
public class CartItemRespDTO implements Serializable {

    /**
     * 商品ID
     */
    private Long productId;

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

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 商品价格
     */
    private java.math.BigDecimal price;

    /**
     * 商品总价（数量×价格）
     */
    private java.math.BigDecimal totalPrice;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 店铺名称
     */
    private String shopName;
}