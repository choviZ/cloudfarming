package com.vv.cloudfarming.product.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车结算分组响应
 */
@Data
public class CartCheckoutGroupRespDTO implements Serializable {

    private Long shopId;

    private String shopName;

    private List<CartItemRespDTO> items;

    private Integer totalQuantity;

    private BigDecimal totalAmount;
}
