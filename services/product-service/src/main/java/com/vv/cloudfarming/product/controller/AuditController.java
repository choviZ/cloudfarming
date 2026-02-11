package com.vv.cloudfarming.product.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.product.dao.entity.AuditRecordDO;
import com.vv.cloudfarming.product.dto.req.AuditApproveReqDTO;
import com.vv.cloudfarming.product.dto.req.AuditPageQueryReqDTO;
import com.vv.cloudfarming.product.dto.req.AuditRejectReqDTO;
import com.vv.cloudfarming.product.dto.req.AuditSubmitReqDTO;
import com.vv.cloudfarming.product.dto.resp.AuditRecordRespDTO;
import com.vv.cloudfarming.product.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 审核控制层
 */
@Tag(name = "审核控制层")
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @Operation(summary = "提交审核")
    @PostMapping("/submit")
    public Result<Long> submitAudit(@Valid @RequestBody AuditSubmitReqDTO requestParam) {
        Long submitterId = StpUtil.getLoginIdAsLong();
        Long auditId = auditService.submitAudit(submitterId, requestParam);
        return Results.success(auditId);
    }

    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @Operation(summary = "分页查询审核记录")
    @PostMapping("/page")
    public Result<IPage<AuditRecordRespDTO>> pageAuditRecords(@RequestBody AuditPageQueryReqDTO requestParam) {
        IPage<AuditRecordRespDTO> pageResult = auditService.pageAuditRecords(requestParam);
        return Results.success(pageResult);
    }

    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @Operation(summary = "审核通过")
    @PostMapping("/approve")
    public Result<Void> approveAudit(@Valid @RequestBody AuditApproveReqDTO requestParam) {
        Long auditorId = StpUtil.getLoginIdAsLong();
        auditService.approveAudit(auditorId, requestParam);
        return Results.success();
    }

    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @Operation(summary = "审核拒绝")
    @PostMapping("/reject")
    public Result<Void> rejectAudit(@Valid @RequestBody AuditRejectReqDTO requestParam) {
        Long auditorId = StpUtil.getLoginIdAsLong();
        auditService.rejectAudit(auditorId, requestParam);
        return Results.success();
    }

    @Operation(summary = "根据商品ID查询最新审核记录")
    @GetMapping("/latest")
    public Result<AuditRecordRespDTO> getLatestAuditRecord(
            @RequestParam("bizType") Integer bizType,
            @RequestParam("bizId") Long bizId) {
        AuditRecordDO record = auditService.getLatestAuditRecord(bizType, bizId);
        if (record == null) {
            return Results.success(null);
        }
        AuditRecordRespDTO result = AuditRecordRespDTO.builder()
                .id(record.getId())
                .bizType(record.getBizType())
                .bizId(record.getBizId())
                .auditStatus(record.getAuditStatus())
                .submitterId(record.getSubmitterId())
                .auditorId(record.getAuditorId())
                .auditTime(record.getAuditTime())
                .rejectReason(record.getRejectReason())
                .createTime(record.getCreateTime())
                .build();
        return Results.success(result);
    }
}
