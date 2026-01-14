package com.vv.cloudfarming.shop.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.cloudfarming.common.database.BaseDO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 认养项目实体类
 */
@Data
@TableName("t_adopt_item")
public class AdoptItemDO extends BaseDO implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发布者用户ID
     */
    private Long userId;

    /**
     * 认养标题
     */
    private String title;

    /**
     * 动物分类（代码）
     */
    private String animalCategory;

    /**
     * 认养周期（天）
     */
    private Integer adoptDays;

    /**
     * 认养价格
     */
    private BigDecimal price;

    /**
     * 预计收益（用户自填，如10kg肉）
     */
    private String expectedYield;

    /**
     * 认养说明
     */
    private String description;

    /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 审核状态：0=待审核 1=通过 2=拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核说明
     */
    private String reviewText;

    /**
     * 上架状态：1=上架 0=下架
     */
    private Integer status;
}
