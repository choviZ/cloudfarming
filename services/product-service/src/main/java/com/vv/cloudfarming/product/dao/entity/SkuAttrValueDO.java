package com.vv.cloudfarming.product.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

/**
 * SKU属性值关联表
 */
@Data
@Builder
@TableName("t_sku_attr_value")
public class SkuAttrValueDO {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标准库存id
     */
    private Long skuId;

    /**
     * 属性id
     */
    private Long attrId;

    /**
     * 属性值
     */
    private String attrValue;

}