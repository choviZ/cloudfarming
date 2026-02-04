package com.vv.cloudfarming.order.dto.common;

import lombok.Data;

@Data
public class ItemDTO {
    /**
     * 商品类型（农产品 / 养殖项目）
     */
    private Integer bizType;

    /**
     * skuId / 养殖项目id
     */
    private Long bizId;

    /**
     * 店铺id
     */
    private Long shopId;

    /**
     * 下单数量
     */
    private Integer quantity;
}
