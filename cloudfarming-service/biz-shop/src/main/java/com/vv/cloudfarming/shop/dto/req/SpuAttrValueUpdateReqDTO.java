package com.vv.cloudfarming.shop.dto.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新SPU属性值请求参数
 */
@Data
public class SpuAttrValueUpdateReqDTO implements Serializable {

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
