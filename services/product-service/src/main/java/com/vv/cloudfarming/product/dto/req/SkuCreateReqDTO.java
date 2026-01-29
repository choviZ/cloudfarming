package com.vv.cloudfarming.product.dto.req;

import com.vv.cloudfarming.product.dto.domain.SaleAttrDTO;
import com.vv.cloudfarming.product.dto.domain.SkuItemDTO;
import lombok.Data;

import java.util.List;

/**
 * 创建sku请求参数
 * 一个请求示例：
 * {
 * "spuId": 1001,
 * "saleAttrs": [
 * {
 * "attrId": 1,
 * "values": ["3斤", "5斤"]
 * },
 * {
 * "attrId": 2,
 * "values": ["30mm", "50mm", "70mm"]
 * }
 * ],
 * "skuItems": [
 * {
 * "attrValues": { "1": "3斤", "2": "30mm" },
 * "price": 18.00,
 * "stock": 100
 * },
 * {
 * "attrValues": { "1": "3斤", "2": "50mm" },
 * "price": 20.00,
 * "stock": 80
 * }省略...
 * ]
 * }
 */
@Data
public class SkuCreateReqDTO {

    /**
     * 关联SPU ID
     */
    private Long spuId;

    /**
     * 销售属性集合
     */
    private List<SaleAttrDTO> saleAttrs;

    /**
     * 每个 SKU 的业务数据
     */
    private List<SkuItemDTO> skuItems;
}
