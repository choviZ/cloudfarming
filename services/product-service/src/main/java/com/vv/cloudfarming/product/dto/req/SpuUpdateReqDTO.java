package com.vv.cloudfarming.product.dto.req;

import com.vv.cloudfarming.product.dto.domain.SaleAttrDTO;
import com.vv.cloudfarming.product.dto.domain.SkuItemDTO;
import com.vv.cloudfarming.product.dto.domain.SpuAttrItemDTO;
import lombok.Data;

import java.util.List;

@Data
public class SpuUpdateReqDTO {

    private Long id;

    private String title;

    private Long categoryId;

    private String images;

    /**
     * 基础属性
     */
    private List<SpuAttrItemDTO> attrItems;

    /**
     * 销售属性
     */
    private List<SaleAttrDTO> saleAttrs;

    /**
     * SKU 列表
     */
    private List<SkuItemDTO> skuItems;
}
