// 请求方法
import request from '../request.ts'
import type { Result, IPage } from '../common'
import type {
  SpuRespDTO,
  SpuDetailRespDTO,
  SpuCreateOrUpdateReqDTO,
  SpuPageQueryReqDTO,
  SpuAttrValueCreateReqDTO,
  SpuAttrValueUpdateReqDTO,
  SpuAttrValueRespDTO
} from './types'

/**
 * 创建或修改SPU
 */
export const saveSpu = (data: SpuCreateOrUpdateReqDTO): Promise<Result<string>> => {
  return request.post('/api/spu/save', data)
}

/**
 * 根据id删除SPU
 */
export const deleteSpuById = (id: number): Promise<Result<void>> => {
  return request.post(`/api/spu/delete${id}`)
}

/**
 * 根据id获取单个SPU详情
 */
export const getSpuDetail = (id: number): Promise<Result<SpuDetailRespDTO>> => {
  return request.get(`/api/spu/get/${id}`)
}

/**
 * 分页查询SPU列表
 */
export const listSpuByPage = (data: SpuPageQueryReqDTO): Promise<Result<IPage<SpuRespDTO>>> => {
  return request.post('/api/spu/page', data)
}

/**
 * 更新SPU状态
 */
export const updateSpuStatus = (id: number, status: number): Promise<Result<void>> => {
  return request.put(`/api/spu/status/${id}`, null, {
    params: { status }
  })
}

/**
 * 创建SPU属性值
 */
export const createSpuAttrValue = (data: SpuAttrValueCreateReqDTO): Promise<Result<void>> => {
  return request.post('/api/spu/attr/create', data)
}

/**
 * 批量创建SPU属性值
 */
export const batchCreateSpuAttrValues = (data: SpuAttrValueCreateReqDTO[]): Promise<Result<void>> => {
  return request.post('/api/spu/attr/create/batch', data)
}

/**
 * 更新SPU属性值
 */
export const updateSpuAttrValue = (data: SpuAttrValueUpdateReqDTO): Promise<Result<boolean>> => {
  return request.post(`/api/spu/attr/update`, data)
}

/**
 * 删除SPU属性值
 */
export const deleteSpuAttrValue = (id: number): Promise<Result<boolean>> => {
  return request.post(`/api/spu/attr/delete/${id}`)
}

/**
 * 批量删除SPU属性值
 */
export const batchDeleteSpuAttrValues = (ids: number[]): Promise<Result<boolean>> => {
  return request.post('/api/spu/attr/delete/batch', ids)
}

/**
 * 根据SPU ID删除所有属性值
 */
export const deleteSpuAttrValuesBySpuId = (spuId: number): Promise<Result<boolean>> => {
  return request.post(`/api/spu/attr/delete/all/${spuId}`)
}

/**
 * 根据ID查询SPU属性值
 */
export const getSpuAttrValueById = (id: number): Promise<Result<SpuAttrValueRespDTO>> => {
  return request.get(`/api/spu/attr/get/${id}`)
}

/**
 * 根据SPU ID查询属性值列表
 */
export const listSpuAttrValuesBySpuId = (spuId: number): Promise<Result<SpuAttrValueRespDTO[]>> => {
  return request.get('/v1/spu/attr/ list', { params: { spuId } })
}

/**
 * 根据SPU ID和属性ID查询属性值
 */
export const getSpuAttrValueBySpuIdAndAttrId = (
  spuId: number,
  attrId: number
): Promise<Result<SpuAttrValueRespDTO>> => {
  return request.get('/v1/spu/attr/id', { params: { spuId, attrId } })
}

// 导出类型
export * from './types'
