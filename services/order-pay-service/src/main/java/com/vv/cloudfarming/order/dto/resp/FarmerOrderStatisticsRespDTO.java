package com.vv.cloudfarming.order.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
}
