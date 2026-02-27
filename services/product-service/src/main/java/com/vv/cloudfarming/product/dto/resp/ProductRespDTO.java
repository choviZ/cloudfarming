package com.vv.cloudfarming.product.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品详情出参
 */
@Data
public class ProductRespDTO implements Serializable {

    /**
     * 商品SPU
     */
    private SpuRespDTO productSpu;
    
    /**
     * 商品SKU
     */
    private List<SkuRespDTO> productSkus;
}
