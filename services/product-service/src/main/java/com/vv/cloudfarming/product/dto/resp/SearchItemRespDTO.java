package com.vv.cloudfarming.product.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SearchItemRespDTO {

    /**
     * 业务 ID
     */
    private Long id;

    /**
     * 商品类型：0-认养项目，1-农产品
     */
    private Integer productType;

    /**
     * 商品类型描述
     */
    private String productTypeDesc;

    /**
     * 标题
     */
    private String title;

    /**
     * 展示图
     */
    private String image;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 搜索副标题
     */
    private String subtitle;

    /**
     * 搜索补充描述
     */
    private String description;

    /**
     * 跳转路径
     */
    private String targetPath;
}
