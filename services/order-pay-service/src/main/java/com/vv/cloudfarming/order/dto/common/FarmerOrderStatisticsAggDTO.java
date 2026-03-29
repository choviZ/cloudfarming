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
}
