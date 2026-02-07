package com.vv.cloudfarming.order.dto.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

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
}
