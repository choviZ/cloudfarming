package com.vv.cloudfarming.shop.dto.req;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建认养项目请求DTO
 */
@Data
public class AdoptItemCreateReqDTO {

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
     * 牲畜总数
     */
    private Integer totalCount;
}
