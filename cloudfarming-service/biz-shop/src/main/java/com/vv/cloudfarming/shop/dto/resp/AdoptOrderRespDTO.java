package com.vv.cloudfarming.shop.dto.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 认养订单响应DTO
 */
@Data
public class AdoptOrderRespDTO {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 买家用户ID
     */
    private Long buyerId;

    /**
     * 认养项目ID
     */
    private Long adoptItemId;

    /**
     * 认养价格（下单时快照）
     */
    private BigDecimal price;

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
}
