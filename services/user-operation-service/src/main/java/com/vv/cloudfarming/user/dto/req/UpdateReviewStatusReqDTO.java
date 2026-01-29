package com.vv.cloudfarming.user.dto.req;

import lombok.Data;

/**
 * 修改审核状态请求参数
 */
@Data
public class UpdateReviewStatusReqDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 要修改为的状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}
