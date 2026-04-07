package com.vv.cloudfarming.order.dto.common;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 农户订单统计聚合结果
 */
@Data
public class FarmerOrderStatisticsAggDTO {

    /**
     * 销售额
     */
    private BigDecimal salesAmount;

    /**
     * 订单量
     */
    private Long orderCount;

    /**
     * 今日销售额
     */
    private BigDecimal todaySalesAmount;

    /**
     * 今日订单量
     */
    private Long todayOrderCount;

    /**
     * 近 7 天销售额
     */
    private BigDecimal last7DaysSalesAmount;

    /**
     * 近 7 天订单量
     */
    private Long last7DaysOrderCount;

    /**
     * 近 30 天销售额
     */
    private BigDecimal last30DaysSalesAmount;

    /**
     * 近 30 天订单量
     */
    private Long last30DaysOrderCount;
}
