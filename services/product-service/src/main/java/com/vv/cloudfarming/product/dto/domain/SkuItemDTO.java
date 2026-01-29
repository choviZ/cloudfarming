package com.vv.cloudfarming.product.dto.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * sku业务数据
 */
@Data
public class SkuItemDTO {

    /**
     * 销售属性组合（attrId → value），例如{ "1": "3斤", "2": "30mm" },
     */
    private Map<Long, String> attrValues;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 图片地址（可选）
     */
    private String image;
}
