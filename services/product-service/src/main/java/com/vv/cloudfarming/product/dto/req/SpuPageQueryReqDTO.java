package com.vv.cloudfarming.product.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

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
     * 商品分类id列表
     */
    private List<Long> categoryIds;

    /**
     * 商品状态：0-下架，1-上架，2-待审核
     */
    private Integer status;

    /**
     * 店铺id.
     */
    private Long shopId;

    /**
     * 审核状态
     */
    private Integer auditStatus;
}
