package com.vv.cloudfarming.product.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审核拒绝请求 DTO
 */
@Data
@Schema(description = "审核拒绝请求")
public class AuditRejectReqDTO {

    /**
     * 审核记录ID
     */
    @NotNull(message = "审核记录ID不能为空")
    @Schema(description = "审核记录ID")
    private Long auditId;

    /**
     * 拒绝原因
     */
    @NotBlank(message = "拒绝原因不能为空")
    @Schema(description = "拒绝原因")
    private String rejectReason;
}
