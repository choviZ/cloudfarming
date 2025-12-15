package com.vv.cloudfarming.shop.dto.req;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 修改农产品商品信息请求参数
 */
@Data
public class ProductUpdateReqDTO implements Serializable {

    /**
     * 商品id
     */
    private Long id;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品分类
     */
    private String productCategory;

    /**
     * 产地
     */
    private String originPlace;

    /**
     * 规格（如：5斤装/单果80mm以上）
     */
    private String specification;

    /**
     * 销售价格（元）
     */
    private BigDecimal price;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品图片URL，多个图片用逗号分隔
     */
    private String productImg;
}