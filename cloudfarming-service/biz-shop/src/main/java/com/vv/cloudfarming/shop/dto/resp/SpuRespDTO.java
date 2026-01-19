package com.vv.cloudfarming.shop.dto.resp;

import lombok.Data;

import java.util.Map;

@Data
public class SpuRespDTO {

    /**
     * 标准商品名称
     */
    private String title;

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

    /**
     * 基本属性
     */
    private Map<String,String> attributes;
}
