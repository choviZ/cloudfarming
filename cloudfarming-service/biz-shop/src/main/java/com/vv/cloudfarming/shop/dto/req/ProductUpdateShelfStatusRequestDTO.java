package com.vv.cloudfarming.shop.dto.req;

import lombok.Data;

/**
 * 修改商品上下架状态请求参数
 */
@Data
public class ProductUpdateShelfStatusRequestDTO {

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * true=上架，false=下架
     */
    private Boolean onShelf;
}