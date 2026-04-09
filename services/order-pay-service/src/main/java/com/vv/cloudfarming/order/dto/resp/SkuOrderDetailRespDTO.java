package com.vv.cloudfarming.order.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuOrderDetailRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String orderNo;

    private Long skuId;

    private Long spuId;

    private String skuName;

    private String skuImage;

    private String skuSpecs;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal totalAmount;

    private Date createTime;

    private Date updateTime;

    private Boolean reviewed;

    private Long reviewId;

    private Integer reviewScore;

    private String reviewContent;

    private List<String> reviewImageUrls;

    private Date reviewCreateTime;
}
