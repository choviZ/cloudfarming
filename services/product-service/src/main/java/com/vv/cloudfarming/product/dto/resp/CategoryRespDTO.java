package com.vv.cloudfarming.product.dto.resp;

import com.vv.cloudfarming.starter.database.base.BaseDO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品分类信息
 */
@Data
public class CategoryRespDTO extends BaseDO implements Serializable {

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

    /**
     * 子分类列表
     */
    private List<CategoryRespDTO> children;
}
