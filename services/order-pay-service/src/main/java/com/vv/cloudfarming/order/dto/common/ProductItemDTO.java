package com.vv.cloudfarming.order.dto.common;

import lombok.Data;

@Data
public class ProductItemDTO {

    /**
     * skuId / 养殖项目id
     */
    private Long bizId;

    /**
     * 下单数量
     */
    private Integer quantity;
}
