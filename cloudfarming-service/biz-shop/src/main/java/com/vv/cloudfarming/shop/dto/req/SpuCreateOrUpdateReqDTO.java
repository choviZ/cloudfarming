package com.vv.cloudfarming.shop.dto.req;

import lombok.Data;

/**
 * 创建或修改SPU请求参数
 */
@Data
public class SpuCreateOrUpdateReqDTO {

    /**
     * 标准商品名称
     */
    private String spuName;

    /**
     * 商品分类id
     */
    private Long categoryId;

    /**
     * 商品图片URL，多个图片用逗号分隔
     */
    private String image;

    /**
     * 商品状态：0-下架，1-上架，2-待审核
     */
    private Integer status;
}
