package com.vv.cloudfarming.product.dto.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * SPU 属性项
 */
@Data
public class SpuAttrItemDTO implements Serializable {

    private Long attrId;

    private String attrValue;
}
