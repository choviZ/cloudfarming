package com.vv.cloudfarming.user.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改审核状态请求参数
 */
@Data
public class UpdateReviewStatusReqDTO {

    /**
     * 主键
     */
    @NotNull
    private Long id;

    /**
     * 要修改为的状态
     */
    @NotNull
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
