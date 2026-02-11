package com.vv.cloudfarming.product.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 审核记录响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "审核记录响应")
public class AuditRecordRespDTO {

    /**
     * 审核记录ID
     */
    @Schema(description = "审核记录ID")
    private Long id;

    /**
     * 业务类型：0-云养殖项目，1-农产品
     */
    @Schema(description = "业务类型：0-云养殖项目，1-农产品")
    private Integer bizType;

    /**
     * 业务ID（商品ID）
     */
    @Schema(description = "业务ID（商品ID）")
    private Long bizId;

    /**
     * 商品标题
     */
    @Schema(description = "商品标题")
    private String bizTitle;

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
     * 审核人ID
     */
    @Schema(description = "审核人ID")
    private Long auditorId;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private Date auditTime;

    /**
     * 拒绝原因
     */
    @Schema(description = "拒绝原因")
    private String rejectReason;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
}
