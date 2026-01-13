import request from '../request.ts'
import type { Result } from '../../types/common'
import type { SkuCreateReqDTO } from '../../types/sku'

/**
 * 创建SKU
 */
export const createSku = (data: SkuCreateReqDTO): Promise<Result<void>> => {
  return request.post('/v1/sku', data)
}
