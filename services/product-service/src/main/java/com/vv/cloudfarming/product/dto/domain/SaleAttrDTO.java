package com.vv.cloudfarming.product.dto.domain;

import lombok.Data;

import java.util.List;

/**
 * 销售属性DTO
 */
@Data
public class SaleAttrDTO {

    private Long attrId;

    private List<String> values;
}
