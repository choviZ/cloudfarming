package com.vv.cloudfarming.order.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.vv.cloudfarming.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 主订单表 DO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value = "t_order_main")
public class OrderDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 主订单号
     */
    private String orderNo;

    /**
     * 下单用户ID
     */
    private Long userId;

    /**
     * 所有子订单商品总价之和
     */
    private BigDecimal totalAmount;

    /**
     * 主订单总优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 主订单总运费
     */
    private BigDecimal freightAmount;

    /**
     * 实付金额
     */
    private BigDecimal actualPayAmount;

    /**
     * 支付方式：1-微信支付 2-支付宝 3-银行卡 4-分期支付（NULL表示未支付）
     */
    private Integer payType;

    /**
     * 支付状态：0-未支付 1-已支付 2-退款中 3-部分退款 4-全部退款 5-支付失败
     */
    private Integer payStatus;

    /**
     * 支付完成时间
     */
    private LocalDateTime payTime;

    /**
     * 收货人姓名
     */
    private String receiveName;

    /**
     * 收货人手机号
     */
    private String receivePhone;

    /**
     * 收货省份
     */
    private String receiveProvince;

    /**
     * 收货城市
     */
    private String receiveCity;

    /**
     * 收货区县
     */
    private String receiveDistrict;

    /**
     * 收货详细地址
     */
    private String receiveDetail;

    /**
     * 主订单状态
     */
    private Integer orderStatus;

    /**
     * 订单类型
     */
    private Integer orderType;
}
