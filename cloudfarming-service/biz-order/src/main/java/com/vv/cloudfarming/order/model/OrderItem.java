package com.vv.cloudfarming.order.model;

import lombok.Data;

@Data
public class OrderItem {
    /**
     * 商品SKU ID
     */
    private Long skuId;

    /**
     * 购买数量
     */
    private Integer quantity;
}
