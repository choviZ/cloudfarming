package com.vv.cloudfarming.order.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSkuReviewRespDTO {

    private Long id;

    private String orderNo;

    private Long orderDetailSkuId;

    private Long spuId;

    private Long skuId;

    private Long shopId;

    private Long userId;

    private Integer score;

    private String content;

    private List<String> imageUrls;

    private String userNameSnapshot;

    private String userAvatarSnapshot;

    private Date createTime;
}
