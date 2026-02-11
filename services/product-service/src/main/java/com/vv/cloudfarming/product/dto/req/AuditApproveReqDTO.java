package com.vv.cloudfarming.product.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审核通过请求 DTO
 */
@Data
@Schema(description = "审核通过请求")
public class AuditApproveReqDTO {

    /**
     * 审核记录ID
     */
    @NotNull(message = "审核记录ID不能为空")
    @Schema(description = "审核记录ID")
    private Long auditId;
}
