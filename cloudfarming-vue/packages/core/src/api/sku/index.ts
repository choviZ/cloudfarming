// 请求方法
import request from '../request.ts'
import type { Result } from '../common'
import type { SkuCreateReqDTO } from './types'

/**
 * 创建SKU
 */
export const createSku = (data: SkuCreateReqDTO): Promise<Result<void>> => {
  return request.post('/v1/sku', data)
}

// 导出类型
export * from './types'
