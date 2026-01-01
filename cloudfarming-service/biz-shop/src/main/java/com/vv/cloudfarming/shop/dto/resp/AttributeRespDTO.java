package com.vv.cloudfarming.shop.dto.resp;

import com.vv.cloudfarming.common.database.BaseDO;
import lombok.Data;

import java.io.Serializable;

/**
 * 属性信息
 */
@Data
public class AttributeRespDTO extends BaseDO implements Serializable {

    /**
     * 属性ID
     */
    private Long id;

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
