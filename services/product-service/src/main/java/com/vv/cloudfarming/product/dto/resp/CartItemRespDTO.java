package com.vv.cloudfarming.product.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车项响应
 */
@Data
public class CartItemRespDTO implements Serializable {

    private Long skuId;

    private Long spuId;

    private Long shopId;

    private String shopName;

    private String productName;

    private String productImage;

    private BigDecimal price;

    private Integer quantity;

    private Boolean selected;

    private Boolean canCheckout;

    private String invalidReason;

    private BigDecimal totalPrice;
}
