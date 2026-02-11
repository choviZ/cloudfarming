package com.vv.cloudfarming.product.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交审核请求 DTO
 */
@Data
@Schema(description = "提交审核请求")
public class AuditSubmitReqDTO {

    /**
     * 业务类型：0-云养殖项目，1-农产品
     */
    @NotNull(message = "业务类型不能为空")
    @Schema(description = "业务类型：0-云养殖项目，1-农产品")
    private Integer bizType;

    /**
     * 业务ID（商品ID）
     */
    @NotNull(message = "业务ID不能为空")
    @Schema(description = "业务ID（商品ID）")
    private Long bizId;
}
