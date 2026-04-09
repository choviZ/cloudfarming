package com.vv.cloudfarming.order.dto.common;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderProductSummaryRecordDTO {

    private String orderNo;

    private Long spuId;

    private Long adoptItemId;

    private String productName;

    private String coverImage;

    private BigDecimal price;

    private Integer quantity;
}
