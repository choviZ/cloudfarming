import request from '../request'
import type { Result } from '../common'
import type { AnimalCategoryResp } from './types'

/**
 * 获取动物分类列表
 */
export function listAnimalCategories() : Promise<Result<AnimalCategoryResp[]>>{
  return request.get(
    '/v1/adopt/animal-category/list'
  )
}