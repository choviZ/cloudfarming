package com.vv.cloudfarming.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.product.dao.entity.AuditRecordDO;
import com.vv.cloudfarming.product.dto.req.AuditApproveReqDTO;
import com.vv.cloudfarming.product.dto.req.AuditPageQueryReqDTO;
import com.vv.cloudfarming.product.dto.req.AuditRejectReqDTO;
import com.vv.cloudfarming.product.dto.req.AuditSubmitReqDTO;
import com.vv.cloudfarming.product.dto.resp.AuditRecordRespDTO;

/**
 * 审核服务接口层
 */
public interface AuditService extends IService<AuditRecordDO> {

    /**
     * 提交审核
     *
     * @param submitterId 提交人ID
     * @param requestParam 请求参数
     * @return 审核记录ID
     */
    Long submitAudit(Long submitterId, AuditSubmitReqDTO requestParam);

    /**
     * 分页查询审核记录
     *
     * @param requestParam 请求参数
     * @return 分页结果
     */
    IPage<AuditRecordRespDTO> pageAuditRecords(AuditPageQueryReqDTO requestParam);

    /**
     * 审核通过
     *
     * @param auditorId 审核人ID
     * @param requestParam 请求参数
     */
    void approveAudit(Long auditorId, AuditApproveReqDTO requestParam);

    /**
     * 审核拒绝
     *
     * @param auditorId 审核人ID
     * @param requestParam 请求参数
     */
    void rejectAudit(Long auditorId, AuditRejectReqDTO requestParam);

    /**
     * 根据业务ID和业务类型查询最新的审核记录
     *
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @return 审核记录
     */
    AuditRecordDO getLatestAuditRecord(Integer bizType, Long bizId);

    /**
     * 更新业务表的审核状态
     *
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param auditStatus 审核状态
     */
    void updateBizAuditStatus(Integer bizType, Long bizId, Integer auditStatus);
}
