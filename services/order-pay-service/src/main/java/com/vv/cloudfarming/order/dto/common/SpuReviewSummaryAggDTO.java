package com.vv.cloudfarming.order.dto.common;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpuReviewSummaryAggDTO {

    private Long totalReviewCount;

    private BigDecimal avgScore;

    private Long score1Count;

    private Long score2Count;

    private Long score3Count;

    private Long score4Count;

    private Long score5Count;
}
