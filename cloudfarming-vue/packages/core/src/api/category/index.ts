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
  return request.post('/api/category/create', data)
}

/**
 * 更新商品分类
 */
export const updateCategory = (data: CategoryUpdateReqDTO): Promise<Result<boolean>> => {
  return request.put('/api/category/update', data)
}

/**
 * 删除商品分类
 */
export const deleteCategory = (id: string): Promise<Result<boolean>> => {
  return request.delete('/api/category/delete', { params: { id } })
}

/**
 * 根据ID查询分类详情
 */
export const getCategoryById = (id: string): Promise<Result<CategoryRespDTO>> => {
  return request.get('/api/category/get', { params: { id } })
}

/**
 * 获取分类树（支持多级分类）
 */
export const getCategoryTree = (): Promise<Result<CategoryRespDTO[]>> => {
  return request.get('/api/category/tree')
}

/**
 * 获取所有顶级分类
 */
export const getTopLevelCategories = (): Promise<Result<CategoryRespDTO[]>> => {
  return request.get('/api/category/top-level')
}

/**
 * 根据父级ID查询子分类
 */
export const getChildrenByParentId = (parentId: string): Promise<Result<CategoryRespDTO[]>> => {
  return request.get('/api/category/children', { params: { parentId } })
}

// 导出类型
export * from './types'
