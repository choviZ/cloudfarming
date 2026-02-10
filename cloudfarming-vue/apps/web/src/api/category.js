import request from './request'

/**
 * 创建商品分类
 * @param {Object} data - CategoryCreateReqDTO
 * @returns {Promise<Object>} Result<void>
 */
export const createCategory = (data) => {
  return request.post('/api/category/create', data)
}

/**
 * 更新商品分类
 * @param {Object} data - CategoryUpdateReqDTO
 * @returns {Promise<Object>} Result<boolean>
 */
export const updateCategory = (data) => {
  return request.put('/api/category/update', data)
}

/**
 * 删除商品分类
 * @param {string} id
 * @returns {Promise<Object>} Result<boolean>
 */
export const deleteCategory = (id) => {
  return request.delete('/api/category/delete', { params: { id } })
}

/**
 * 根据ID查询分类详情
 * @param {string} id
 * @returns {Promise<Object>} Result<CategoryRespDTO>
 */
export const getCategoryById = (id) => {
  return request.get('/api/category/get', { params: { id } })
}

/**
 * 获取分类树（支持多级分类）
 * @returns {Promise<Object>} Result<CategoryRespDTO[]>
 */
export const getCategoryTree = () => {
  return request.get('/api/category/tree')
}

/**
 * 获取所有顶级分类
 * @returns {Promise<Object>} Result<CategoryRespDTO[]>
 */
export const getTopLevelCategories = () => {
  return request.get('/api/category/top-level')
}

/**
 * 根据父级ID查询子分类
 * @param {string} parentId
 * @returns {Promise<Object>} Result<CategoryRespDTO[]>
 */
export const getChildrenByParentId = (parentId) => {
  return request.get('/api/category/children', { params: { parentId } })
}
