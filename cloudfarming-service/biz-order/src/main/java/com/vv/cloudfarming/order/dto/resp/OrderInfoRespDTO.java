package com.vv.cloudfarming.order.dto.resp;

import com.vv.cloudfarming.order.dto.ProductInfoDTO;
import com.vv.cloudfarming.order.dto.ReceiveInfoDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单信息响应
 */
@Data
@Builder
public class OrderInfoRespDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 主订单号
     */
    private String orderNo;

    /**
     * 商品列表
     */
    private List<ProductInfoDTO> productList;

    /**
     * 总价
     */
    private BigDecimal totalAmount;

    /**
     * 总优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 总运费
     */
    private BigDecimal freightAmount;

    /**
     * 实付金额
     */
    private BigDecimal actualPayAmount;

    /**
     * 支付方式
     */
    private Integer payType;

    /**
     * 支付状态
     */
    private Integer payStatus;

    /**
     * 主订单状态
     */
    private Integer orderStatus;

    /**
     * 收货信息
     */
    private ReceiveInfoDTO receiveInfoDTO;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 付款时间
     */
    private LocalDateTime payTime;

    /**
     * 成交时间
     */
    private LocalDateTime closeTime;

}
