package com.vv.cloudfarming.product.dto.req;

import lombok.Data;

@Data
public class SearchPageReqDTO {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 商品类型：0-认养项目，1-农产品，null-全部
     */
    private Integer productType;

    /**
     * 当前页
     */
    private Long current;

    /**
     * 每页条数
     */
    private Long size;
}
