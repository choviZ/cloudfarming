package com.vv.cloudfarming.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * SPU 价格摘要
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpuPriceSummaryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 所属 SPU ID
     */
    private Long spuId;

    /**
     * 最低价格
     */
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    private BigDecimal maxPrice;

    /**
     * 最低价格对应的 SKU ID
     */
    private Long minPriceSkuId;
}
