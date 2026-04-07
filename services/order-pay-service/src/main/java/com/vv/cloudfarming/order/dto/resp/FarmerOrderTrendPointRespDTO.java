package com.vv.cloudfarming.order.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 农户订单趋势点响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerOrderTrendPointRespDTO {

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
