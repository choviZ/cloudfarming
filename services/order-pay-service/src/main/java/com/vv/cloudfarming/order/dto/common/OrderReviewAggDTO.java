package com.vv.cloudfarming.order.dto.common;

import lombok.Data;

@Data
public class OrderReviewAggDTO {

    private String orderNo;

    private Long totalItemCount;

    private Long pendingReviewCount;
}
