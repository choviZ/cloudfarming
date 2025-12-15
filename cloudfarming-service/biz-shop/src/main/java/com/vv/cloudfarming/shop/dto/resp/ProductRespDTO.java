package com.vv.cloudfarming.shop.dto.resp;

import com.vv.cloudfarming.common.database.BaseDO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 农产品商品信息
 */
@Data
public class ProductRespDTO extends BaseDO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 店铺id
     */
    private Long shopId;

    /**
     * 创建人id
     */
    private Long creatorId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品分类
     */
    private String productCategory;

    /**
     * 产地
     */
    private String originPlace;

    /**
     * 规格（如：5斤装/单果80mm以上）
     */
    private String specification;

    /**
     * 销售价格（元）
     */
    private BigDecimal price;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品图片URL，多个图片用逗号分隔
     */
    private String productImg;

    /**
     * 商品状态：0-下架，1-上架，2-待审核
     */
    private Integer status;

    /**
     * 上架时间
     */
    private LocalDateTime shelfTime;

    /**
     * 下架时间
     */
    private LocalDateTime offShelfTime;
}