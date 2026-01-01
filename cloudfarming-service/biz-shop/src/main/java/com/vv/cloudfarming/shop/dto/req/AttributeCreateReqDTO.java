package com.vv.cloudfarming.shop.dto.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建属性请求参数
 */
@Data
public class AttributeCreateReqDTO implements Serializable {

    /**
     * 关联的分类ID
     */
    private Long categoryId;

    /**
     * 属性名
     */
    private String name;

    /**
     * 属性类型：0-基本 / 1-销售
     */
    private Integer attrType;

    /**
     * 排序权重
     */
    private Integer sort;
}
