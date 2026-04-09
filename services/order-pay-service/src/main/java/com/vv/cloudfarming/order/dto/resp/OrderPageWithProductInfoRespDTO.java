package com.vv.cloudfarming.order.dto.resp;

import com.vv.cloudfarming.order.dto.common.ProductSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageWithProductInfoRespDTO {

    private Long id;

    private String orderNo;

    private String payOrderNo;

    private String shopName;

    private List<ProductSummaryDTO> items;

    private BigDecimal totalPrice;

    private BigDecimal totalAmount;

    private BigDecimal actualPayAmount;

    private Integer orderType;

    private Integer orderStatus;

    private Long pendingReviewCount;

    private Boolean allReviewed;

    private Date createTime;
}
