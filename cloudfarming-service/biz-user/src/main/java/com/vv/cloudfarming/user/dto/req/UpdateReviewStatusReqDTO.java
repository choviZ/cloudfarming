package com.vv.cloudfarming.user.dto.req;

import lombok.Data;

/**
 * 修改审核状态请求参数
 */
@Data
public class UpdateReviewStatusReqDTO {

    private Long id;

    private String status;

    private String remark;
}
