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

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String orderNo;

    private Long orderDetailSkuId;

    private Long spuId;

    private Long skuId;

    private Long shopId;

    private Long userId;

    private Integer score;

    private String content;

    private String imageUrls;

    private String userNameSnapshot;

    private String userAvatarSnapshot;
}
