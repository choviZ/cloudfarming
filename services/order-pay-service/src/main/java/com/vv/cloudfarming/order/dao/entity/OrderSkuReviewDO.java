package com.vv.cloudfarming.order.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.cloudfarming.starter.database.base.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_order_sku_review")
public class OrderSkuReviewDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 订单明细SKU ID
     */
    private Long orderDetailSkuId;

    /**
     * 订单明细认养ID
     */
    private Long orderDetailAdoptId;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * 认养项目ID
     */
    private Long adoptItemId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 评分(1-5)
     */
    private Integer score;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价图片URL，多个用逗号分隔
     */
    private String imageUrls;

    /**
     * 用户名快照
     */
    private String userNameSnapshot;

    /**
     * 用户头像快照
     */
    private String userAvatarSnapshot;
}
