package com.vv.cloudfarming.order.dto.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 认养订单响应DTO
 */
@Data
public class AdoptOrderDetailRespDTO {

    /**
     * ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 认养项目ID
     */
    private Long adoptItemId;

    /**
     * 认养项目名称
     */
    private String itemName;

    /**
     * 认养项目图片
     */
    private String itemImage;

    /**
     * 项目单价
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private int quantity;

    /**
     * 总价
     */
    private BigDecimal totalAmount;

    /**
     * 认养开始日期
     */
    private Date startDate;

    /**
     * 认养结束日期
     */
    private Date endDate;

    /**
     * 认养状态：1=认养中 2=已完成 3=已取消
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

    /**
     * 关联的收获地址id
     */
    private Long receiveId;

    /**
     * 是否已评价
     */
    private Boolean reviewed;

    /**
     * 评价ID
     */
    private Long reviewId;

    /**
     * 评价评分
     */
    private Integer reviewScore;

    /**
     * 评价内容
     */
    private String reviewContent;

    /**
     * 评价图片列表
     */
    private List<String> reviewImageUrls;

    /**
     * 评价创建时间
     */
    private Date reviewCreateTime;
}
