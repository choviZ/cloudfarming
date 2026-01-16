// 请求方法
import request from '../request.ts'
import type { Result } from '../common'
import type {
  CategoryRespDTO,
  CategoryCreateReqDTO,
  CategoryUpdateReqDTO
} from './types'

/**
 * 创建商品分类
 */
export const createCategory = (data: CategoryCreateReqDTO): Promise<Result<void>> => {
  return request.post('/v1/category', data)
}

/**
 * 更新商品分类
 */
export const updateCategory = (data: CategoryUpdateReqDTO): Promise<Result<boolean>> => {
  return request.put('/v1/category', data)
}

/**
 * 删除商品分类
 */
export const deleteCategory = (id: string): Promise<Result<boolean>> => {
  return request.delete('/v1/category', { params: { id } })
}

/**
 * 根据ID查询分类详情
 */
export const getCategoryById = (id: string): Promise<Result<CategoryRespDTO>> => {
  return request.get('/v1/category', { params: { id } })
}

/**
 * 获取分类树（支持多级分类）
 */
export const getCategoryTree = (): Promise<Result<CategoryRespDTO[]>> => {
  return request.get('/v1/category/tree')
}

/**
 * 获取所有顶级分类
 */
export const getTopLevelCategories = (): Promise<Result<CategoryRespDTO[]>> => {
  return request.get('/v1/category/top-level')
}

/**
 * 根据父级ID查询子分类
 */
export const getChildrenByParentId = (parentId: string): Promise<Result<CategoryRespDTO[]>> => {
  return request.get('/v1/category/children', { params: { parentId } })
}

// 导出类型
export * from './types'
