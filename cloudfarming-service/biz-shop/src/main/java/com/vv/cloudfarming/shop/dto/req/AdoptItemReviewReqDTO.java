package com.vv.cloudfarming.shop.dto.req;

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
