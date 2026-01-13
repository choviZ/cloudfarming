import request from '../request.ts'
import type { Result } from '../../types/common'
import type {
  SpuRespDTO,
  SpuCreateOrUpdateReqDTO,
  SpuPageQueryReqDTO,
  SpuAttrValueCreateReqDTO,
  SpuAttrValueUpdateReqDTO,
  SpuAttrValueRespDTO
} from '../../types/spu'
import type { IPage } from '../../types/common'

/**
 * 创建或修改SPU
 */
export const saveSpu = (data: SpuCreateOrUpdateReqDTO): Promise<Result<string>> => {
  return request.post('/v1/spu/save-or-update', data)
}

/**
 * 根据id删除SPU
 */
export const deleteSpuById = (id: number): Promise<Result<void>> => {
  return request.delete(`/v1/spu/${id}`)
}

/**
 * 根据id获取单个SPU详情
 */
export const getSpuById = (id: number): Promise<Result<SpuRespDTO>> => {
  return request.get(`/v1/spu/${id}`)
}

/**
 * 分页查询SPU列表
 */
export const listSpuByPage = (data: SpuPageQueryReqDTO): Promise<Result<IPage<SpuRespDTO>>> => {
  return request.post('/v1/spu/page', data)
}

/**
 * 更新SPU状态
 */
export const updateSpuStatus = (id: number, status: number): Promise<Result<void>> => {
  return request.put('/v1/spu/status', null, {
    params: { id, status }
  })
}

/**
 * 创建SPU属性值
 */
export const createSpuAttrValue = (data: SpuAttrValueCreateReqDTO): Promise<Result<void>> => {
  return request.post('/v1/spu/attr', data)
}

/**
 * 批量创建SPU属性值
 */
export const batchCreateSpuAttrValues = (data: SpuAttrValueCreateReqDTO[]): Promise<Result<void>> => {
  return request.post('/v1/spu/attr/batch', data)
}

/**
 * 更新SPU属性值
 */
export const updateSpuAttrValue = (data: SpuAttrValueUpdateReqDTO): Promise<Result<boolean>> => {
  return request.put('/v1/spu/attr', data)
}

/**
 * 删除SPU属性值
 */
export const deleteSpuAttrValue = (id: number): Promise<Result<boolean>> => {
  return request.delete('/v1/spu/attr', { params: { id } })
}

/**
 * 批量删除SPU属性值
 */
export const batchDeleteSpuAttrValues = (ids: number[]): Promise<Result<boolean>> => {
  return request.delete('/v1/spu/attr/batch', { data: ids })
}

/**
 * 根据SPU ID删除所有属性值
 */
export const deleteSpuAttrValuesBySpuId = (spuId: number): Promise<Result<boolean>> => {
  return request.delete('/v1/spu/attr/all', { params: { spuId } })
}

/**
 * 根据ID查询SPU属性值
 */
export const getSpuAttrValueById = (id: number): Promise<Result<SpuAttrValueRespDTO>> => {
  return request.get('/v1/spu/attr', { params: { id } })
}

/**
 * 根据SPU ID查询属性值列表
 */
export const listSpuAttrValuesBySpuId = (spuId: number): Promise<Result<SpuAttrValueRespDTO[]>> => {
  return request.get('/v1/spu/attr/list', { params: { spuId } })
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
