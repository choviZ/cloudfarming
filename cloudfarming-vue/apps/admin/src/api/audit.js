import request from './request'

/**
 * 查询业务的最新审核记录
 * @param {Object} params - { bizType, bizId }
 * @returns {Promise<Object>} Result<AuditRecordRespDTO>
 */
export const getLatestAuditRecord = (params) => {
  return request.get('/api/audit/latest', { params })
}

/**
 * 审核通过
 * @param {number} auditId
 * @returns {Promise<Object>} Result<void>
 */
export const approveAudit = (auditId) => {
  return request.post('/api/audit/approve', { auditId })
}

/**
 * 审核拒绝
 * @param {number} auditId
 * @param {string} rejectReason
 * @returns {Promise<Object>} Result<void>
 */
export const rejectAudit = (auditId, rejectReason) => {
  return request.post('/api/audit/reject', {
    auditId,
    rejectReason
  })
}
