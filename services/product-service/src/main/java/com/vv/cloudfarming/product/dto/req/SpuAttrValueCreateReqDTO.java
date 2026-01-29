package com.vv.cloudfarming.product.dto.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建SPU属性值请求参数
 */
@Data
public class SpuAttrValueCreateReqDTO implements Serializable {

    /**
     * 标准商品ID
     */
    private Long spuId;

    /**
     * 属性ID
     */
    private Long attrId;

    /**
     * 属性值
     */
    private String attrValue;
}
