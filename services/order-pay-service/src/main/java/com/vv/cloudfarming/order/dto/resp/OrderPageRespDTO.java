package com.vv.cloudfarming.order.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单响应数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
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
     * 订单状态
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
     * 收货地址详情
     */
    private String receiveAddress;

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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
