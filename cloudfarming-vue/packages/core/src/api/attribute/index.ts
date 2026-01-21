// 请求方法
import request from '../request.ts'
import type { Result } from '../common'
import type {
  AttributeCreateReqDTO,
  AttributeRespDTO,
  AttributeUpdateReqDTO,
  AttributeValueRespDTO
} from './types'

/**
 * 根据属性ID查询属性值列表
 */
export const getAttributeValuesByAttrId = (attributeId: string): Promise<Result<AttributeValueRespDTO[]>> => {
  return request.get('/v1/attribute-value/by-attr', { params: { attributeId } })
}

/**
 * 创建属性
 */
export const createAttribute = (requestParam: AttributeCreateReqDTO): Promise<Result<void>> => {
  return request.post('/v1/attribute', requestParam)
}

/**
 * 更新属性
 */
export const updateAttribute = (requestParam: AttributeUpdateReqDTO): Promise<Result<boolean>> => {
  return request.put('/v1/attribute', requestParam)
}

/**
 * 删除属性
 */
export const deleteAttribute = (id: string): Promise<Result<boolean>> => {
  return request.delete('/v1/attribute', { params: { id: id } })
}

/**
 * 批量删除属性
 */
export const batchDeleteAttributes = (ids: string[]): Promise<Result<boolean>> => {
  return request.delete('/v1/attribute/batch', {data: ids})
}

/**
 * 根据ID查询属性详情
 */
export const getAttributeById = (id: string): Promise<Result<AttributeRespDTO>> => {
  return request.get('/v1/attribute', { params: { id: id } })
}

/**
 * 根据分类ID查询属性列表
 */
export const getAttributesByCategoryId = (categoryId: string): Promise<Result<AttributeRespDTO[]>> => {
  return request.get('/v1/attribute/by-category', { params: { categoryId: categoryId } })
}

/**
 * 根据分类ID和属性类型查询属性列表
 */
export const getAttributesByCategoryIdAndType = (categoryId: string, attrType: number): Promise<Result<AttributeRespDTO[]>> => {
  return request.get('/v1/attribute/by-category-and-type', { params: { categoryId: categoryId, attrType: attrType } })
}


// 导出类型
export * from './types'
