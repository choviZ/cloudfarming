package com.vv.cloudfarming.product.dto.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建商品分类请求参数
 */
@Data
public class CategoryCreateReqDTO implements Serializable {

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父级分类ID，顶级分类为NULL
     */
    private Long parentId;

    /**
     * 排序权重
     */
    private Integer sortOrder;
}
