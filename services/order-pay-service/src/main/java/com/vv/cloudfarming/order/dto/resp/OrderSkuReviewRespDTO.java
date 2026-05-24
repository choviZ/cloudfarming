package com.vv.cloudfarming.order.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSkuReviewRespDTO {

    /**
     * 评价ID
     */
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
     * 评价图片列表
     */
    private List<String> imageUrls;

    /**
     * 用户名快照
     */
    private String userNameSnapshot;

    /**
     * 用户头像快照
     */
    private String userAvatarSnapshot;

    /**
     * 创建时间
     */
    private Date createTime;
}
