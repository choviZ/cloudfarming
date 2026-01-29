package com.vv.cloudfarming.product.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * SKU 详情响应对象
 */
@Data
public class SkuRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    private Long id;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * 店铺 ID
     */
    private Long shopId;

    /**
     * 商品标题 (来自 SPU)
     */
    private String spuTitle;

    /**
     * 商品图片 (来自 SPU，通常取第一张)
     */
    private String spuImage;

    /**
     * 销售价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * SKU 状态：0-下架，1-上架
     */
    private Integer status;

    /**
     * 销售属性集合 (key: 属性名, value: 属性值)
     * 例如: {"颜色": "红色", "尺寸": "L"}
     */
    private Map<String, String> saleAttrs;
}
