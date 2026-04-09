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
     * 商品SPU ID（认养条目为空）
     */
    private Long spuId;

    /**
     * 认养项目ID（商品条目为空）
     */
    private Long adoptItemId;

    /**
     * 商品名称
     */
    private String productName;

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
