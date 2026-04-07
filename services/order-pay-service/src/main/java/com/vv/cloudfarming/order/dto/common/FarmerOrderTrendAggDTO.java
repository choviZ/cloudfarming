package com.vv.cloudfarming.order.dto.common;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 农户订单趋势聚合结果
 */
@Data
public class FarmerOrderTrendAggDTO {

    /**
     * 统计日期
     */
    private String statDate;

    /**
     * 销售额
     */
    private BigDecimal salesAmount;

    /**
     * 订单量
     */
    private Long orderCount;
}
