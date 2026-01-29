package com.vv.cloudfarming.product.dto.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新商品分类请求参数
 */
@Data
public class CategoryUpdateReqDTO implements Serializable {

    /**
     * 分类ID
     */
    private Long id;

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
