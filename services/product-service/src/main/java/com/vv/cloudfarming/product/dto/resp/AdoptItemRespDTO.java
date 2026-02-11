package com.vv.cloudfarming.product.dto.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 认养项目响应DTO
 */
@Data
public class AdoptItemRespDTO {

    /**
     * 主键ID
     */
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
     * 审核状态：0=待审核 1=通过 2=拒绝
     */
    private Integer auditStatus;

    /**
     * 上架状态：1=上架 0=下架
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
