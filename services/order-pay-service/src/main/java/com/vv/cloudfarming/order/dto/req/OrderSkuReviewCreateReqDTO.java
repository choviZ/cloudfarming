package com.vv.cloudfarming.order.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderSkuReviewCreateReqDTO {

    private Long orderDetailSkuId;

    private Long orderDetailAdoptId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1分")
    @Max(value = 5, message = "评分最高为5分")
    private Integer score;

    private String content;

    private List<String> imageUrls;
}
