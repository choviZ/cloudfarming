package com.vv.cloudfarming.shop.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * SPU属性值关联表
 */
@Data
@TableName("t_spu_attr_value")
public class SpuAttrValueDO {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标准商品id
     */
    private Long spuId;

    /**
     * 属性id
     */
    private Long attrId;

    /**
     * 属性值
     */
    private String attrValue;
}