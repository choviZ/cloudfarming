package com.vv.cloudfarming.product.dto.req;

import lombok.Data;

/**
 * 创建或修改SPU请求参数
 */
@Data
public class SpuCreateReqDTO {

    /**
     * 标准商品名称
     */
    private String title;

    /**
     * 店铺id
     */
    private Long shopId;

    /**
     * 商品分类id
     */
    private Long categoryId;

    /**
     * 商品图片URL，多个图片用逗号分隔
     */
    private String images;
}
