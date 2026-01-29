package com.vv.cloudfarming.product.dto.req;

import lombok.Data;

/**
 * 审核请求参数
 */
@Data
public class AdoptItemReviewReqDTO {

    private Long id;

    private Integer status;

    private String message;
}
