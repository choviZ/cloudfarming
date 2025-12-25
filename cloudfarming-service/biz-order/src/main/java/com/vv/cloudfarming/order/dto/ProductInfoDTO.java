package com.vv.cloudfarming.order.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品信息
 */
@Data
@Builder
public class ProductInfoDTO {

    private Long productId;

    private String productName;

    private String productImg;

    private String description;

    private BigDecimal price;

    private int quantity;
}
