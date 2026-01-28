package com.vv.cloudfarming.order.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付单关联的商品信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayOrderItemRespDTO {

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 类型
     */
    private Integer productType;

    /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 下单数量
     */
    private Integer quantity;
}
