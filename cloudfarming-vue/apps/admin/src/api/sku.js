import request from './request'

/**
 * 创建SKU
 * @param {Object} data - SkuCreateReqDTO
 * @returns {Promise<Object>} Result<void>
 */
export const createSku = (data) => {
  return request.post('/api/sku/create', data)
}
