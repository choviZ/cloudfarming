package com.vv.cloudfarming.order.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 农户订单统计响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerOrderStatisticsRespDTO {

    /**
     * 店铺 ID
     */
    private Long shopId;

    /**
     * 店铺名称
     */
    private String shopName;

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

    /**
     * 近 30 天趋势数据
     */
    private List<FarmerOrderTrendPointRespDTO> trendPoints;
}
