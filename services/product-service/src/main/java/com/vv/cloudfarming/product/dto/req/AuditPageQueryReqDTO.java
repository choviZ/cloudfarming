package com.vv.cloudfarming.product.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 审核记录分页查询请求 DTO
 */
@Data
@Schema(description = "审核记录分页查询请求")
public class AuditPageQueryReqDTO {

    /**
     * 当前页码
     */
    @Min(value = 1, message = "页码必须大于0")
    @Schema(description = "当前页码", defaultValue = "1")
    private Integer current = 1;

    /**
     * 每页条数
     */
    @Min(value = 1, message = "每页条数必须大于0")
    @Schema(description = "每页条数", defaultValue = "10")
    private Integer size = 10;

    /**
     * 业务类型：0-云养殖项目，1-农产品
     */
    @Schema(description = "业务类型：0-云养殖项目，1-农产品")
    private Integer bizType;

    /**
     * 审核状态：0-待审核，1-通过，2-拒绝
     */
    @Schema(description = "审核状态：0-待审核，1-通过，2-拒绝")
    private Integer auditStatus;

    /**
     * 提交人ID
     */
    @Schema(description = "提交人ID")
    private Long submitterId;

    /**
     * 商品标题（模糊查询）
     */
    @Schema(description = "商品标题（模糊查询）")
    private String titleKeyword;
}
