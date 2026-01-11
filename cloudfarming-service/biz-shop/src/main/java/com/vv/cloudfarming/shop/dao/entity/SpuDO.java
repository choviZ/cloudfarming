package com.vv.cloudfarming.shop.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.vv.cloudfarming.common.database.BaseDO;
import lombok.Data;

import java.io.Serializable;

/**
 * 农产品商品SPU表
 */
@Data
@TableName("t_spu")
public class SpuDO extends BaseDO implements Serializable {

    /**
     * SPU唯一主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属店铺id
     */
    private Long shopId;

    /**
     * 标准商品标题
     */
    private String title;

    /**
     * 商品分类id
     */
    private Long categoryId;

    /**
     * 商品图片URL，多个图片用逗号分隔
     */
    private String image;

    /**
     * 商品状态：0-下架，1-上架，2-待审核
     */
    private Integer status;
}