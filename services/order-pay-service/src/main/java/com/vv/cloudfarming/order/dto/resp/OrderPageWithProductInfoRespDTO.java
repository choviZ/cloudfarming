package com.vv.cloudfarming.order.dto.resp;

import com.vv.cloudfarming.order.dto.common.ProductSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户查询订单列表返回的数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageWithProductInfoRespDTO {

    /**
     * 订单id
     */
    private Long id;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 商品信息
     */
    private List<ProductSummaryDTO> items;

    /**
     * 订单总价
     */
    private BigDecimal totalPrice;
}
