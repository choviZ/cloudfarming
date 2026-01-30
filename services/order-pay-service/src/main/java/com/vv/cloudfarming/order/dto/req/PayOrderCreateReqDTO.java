package com.vv.cloudfarming.order.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderCreateReqDTO {

    private Long buyerId;

    private String payOrderNo;

    private BigDecimal totalAmount;
}
