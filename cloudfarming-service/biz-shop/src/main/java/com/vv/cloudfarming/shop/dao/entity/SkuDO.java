package com.vv.cloudfarming.shop.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.vv.cloudfarming.common.database.BaseDO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 农产品商品SKU表
 */
@Data
@TableName("t_sku")
public class SkuDO extends BaseDO implements Serializable {

    /**
     * SKU唯一主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联SPU ID
     */
    private Long spuId;

    /**
     * 所属农户/店铺ID
     */
    private Long shopId;


    /**
     * SKU状态：0-下架，1-上架
     */
    private Integer status;

    /**
     * 销售价格（元）
     */
    private BigDecimal price;

    /**
     * 库存数量
     */
    private Integer stock;
}