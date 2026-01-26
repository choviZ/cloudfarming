package com.vv.cloudfarming.order.dto.req;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayOrderCreateReqDTO {

    private Long buyerId;

    private BigDecimal totalAmount;
}
