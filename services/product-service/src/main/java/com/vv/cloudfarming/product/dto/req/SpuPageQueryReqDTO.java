package com.vv.cloudfarming.product.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
public class SpuPageQueryReqDTO extends Page {

    /**
     * id
     */
    private Long id;

    /**
     * 标准商品名称
     */
    private String spuName;

    /**
     * 商品分类id
     */
    private Long categoryId;

    /**
     * 商品状态：0-下架，1-上架，2-待审核
     */
    private Integer status;
}
