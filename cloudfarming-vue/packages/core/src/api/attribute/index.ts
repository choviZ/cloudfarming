import request from '../request.ts'
import type { Result } from '../../types/common'
import type {
  AttributeRespDTO,
  AttributeValueRespDTO
} from '../../types/attribute'

/**
 * 根据分类ID查询属性列表
 */
export const getAttributesByCategoryId = (categoryId: string): Promise<Result<AttributeRespDTO[]>> => {
  return request.get('/v1/attribute/by-category', { params: { categoryId: categoryId } })
}

/**
 * 根据属性ID查询属性值列表
 */
export const getAttributeValuesByAttrId = (attributeId: string): Promise<Result<AttributeValueRespDTO[]>> => {
  return request.get('/v1/attribute-value/by-attr', { params: { attributeId } })
}
