package com.vv.cloudfarming.order.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.vv.cloudfarming.common.database.BaseDO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 子订单表 DO
 */
@Data
@TableName(value = "t_order_item")
public class OrderItemDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 子订单号
     */
    private String itemOrderNo;

    /**
     * 主订单表的id
     */
    private Long mainOrderId;

    /**
     * 主订单号
     */
    private String mainOrderNo;

    /**
     * 下单用户ID
     */
    private Long userId;

    /**
     * 供货农户ID
     */
    private Long farmerId;

    /**
     * 该农户的商品列表（JSON格式，存储多个商品的详细属性）
     */
    private String productJson;

    /**
     * 该子订单商品总数量
     */
    private Integer productTotalQuantity;

    /**
     * 该子订单商品总价
     */
    private BigDecimal productTotalAmount;

    /**
     * 分摊的主订单优惠金额
     */
    private BigDecimal shareDiscountAmount;

    /**
     * 子订单的运费
     */
    private BigDecimal shareFreightAmount;

    /**
     * 子订单实付金额
     */
    private BigDecimal actualPayAmount;

    /**
     * 子订单状态
     */
    private Integer orderStatus;

    /**
     * 物流公司名称
     */
    private String logisticsCompany;

    /**
     * 物流单号
     */
    private String logisticsNo;

    /**
     * 物流状态
     */
    private Integer logisticsStatus;

    /**
     * 农户发货时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 用户确认收货时间
     */
    private LocalDateTime receiveTime;

    /**
     * 售后状态
     */
    private Integer afterSaleStatus;
}
