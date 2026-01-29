package com.vv.cloudfarming.order.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单关联的商品信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductSummaryDTO {

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 类型
     */
    private Integer productType;

    /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 下单数量
     */
    private Integer quantity;
}
