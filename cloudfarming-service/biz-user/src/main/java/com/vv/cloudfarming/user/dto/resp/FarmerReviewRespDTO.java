package com.vv.cloudfarming.user.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * 申请入住农户审核状态
 */
@Data
@Builder
public class FarmerReviewRespDTO {

    /**
     * 审核状态
     */
    private String status;

    /**
     * 备注
     */
    private String reviewRemark;
}
