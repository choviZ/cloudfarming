package com.vv.cloudfarming.product.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * SKU 详情响应对象
 */
@Data
public class SkuRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * 商品图片
     */
    private String skuImage;

    /**
     * 销售价格
     */
    private BigDecimal price;

    /**
     * 锁定的库存
     */
    private Integer lockStock;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * SKU 状态：0-下架，1-上架
     */
    private Integer status;

    /**
     * 销售属性 JSON
     */
    private String saleAttribute;
}
