package com.vv.cloudfarming.product.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.cloudfarming.starter.database.base.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 认养项目实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_adopt_item")
public class AdoptItemDO extends BaseDO implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 店铺ID
     */
    private Long shopId;

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
     * 本批次总数
     */
    private Integer totalCount;

    /**
     * 剩余可认养数量
     */
    private Integer availableCount;

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
