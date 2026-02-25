package com.vv.cloudfarming.order.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.vv.cloudfarming.starter.database.base.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表 DO
 * <p>
 * 核心业务订单表，一次下单行为（按店铺/业务）生成一条记录。
 * 支付相关信息（状态、时间、方式）通过 pay_order_no 关联 t_pay_order 获取。
 * 订单自身仅维护 order_status (业务流转状态)。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_order")
public class OrderDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单号 (业务唯一标识)
     */
    private String orderNo;

    /**
     * 支付单号 (关联 t_pay_order)
     */
    private String payOrderNo;

    /**
     * 下单用户ID
     */
    private Long userId;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 订单类型: 1-普通电商, 2-认养项目
     */
    private Integer orderType;

    /**
     * 订单总金额 (商品总价 + 运费 - 优惠)
     */
    private BigDecimal totalAmount;

    /**
     * 实付金额 (应付)
     */
    private BigDecimal actualPayAmount;

    /**
     * 运费
     */
    private BigDecimal freightAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 订单状态: 0-待支付 1-待发货 2-已发货 ...
     */
    private Integer orderStatus;

    /**
     * 收货人姓名
     */
    private String receiveName;

    /**
     * 收货人手机号
     */
    private String receivePhone;

    /**
     * 省份名称
     */
    private String provinceName;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 区县名称
     */
    private String districtName;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 物流单号
     */
    private String logisticsNo;

    /**
     * 物流公司
     */
    private String logisticsCompany;

    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 收货时间
     */
    private LocalDateTime receiveTime;
}
