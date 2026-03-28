package com.vv.cloudfarming.product.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.cloudfarming.starter.database.base.BaseDO;
import lombok.Data;

import java.io.Serializable;

/**
 * 购物车表实体
 */
@Data
@TableName("t_cart_item")
public class CartItemDO extends BaseDO implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 是否选中
     */
    private Boolean selected;
}
