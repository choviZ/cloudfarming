package com.vv.cloudfarming.product.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车结算预览响应
 */
@Data
public class CartCheckoutPreviewRespDTO implements Serializable {

    private List<CartCheckoutGroupRespDTO> groups;

    private Integer totalQuantity;

    private BigDecimal totalAmount;

    private Boolean canSubmit;

    private List<CartItemRespDTO> invalidItems;
}
