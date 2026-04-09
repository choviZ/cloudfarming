package com.vv.cloudfarming.order.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpuReviewSummaryRespDTO {

    private Long totalReviewCount;

    private BigDecimal avgScore;

    private Long score1Count;

    private Long score2Count;

    private Long score3Count;

    private Long score4Count;

    private Long score5Count;
}
