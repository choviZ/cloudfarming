package com.vv.cloudfarming.shop.dto.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * SPU属性值信息
 */
@Data
public class SpuAttrValueRespDTO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

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
