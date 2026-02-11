package com.vv.cloudfarming.product.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.vv.cloudfarming.starter.database.base.BaseDO;
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
    private String images;

    /**
     * 商品状态：0-下架，1-上架，2-待审核
     */
    private Integer status;

    /**
     * 审核状态：0-待审核，1-通过，2-拒绝
     */
    private Integer auditStatus;
}