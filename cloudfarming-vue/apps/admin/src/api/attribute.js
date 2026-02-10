import request from './request'

/**
 * 根据属性ID查询属性值列表
 */
export const getAttributeValuesByAttrId = (attributeId) => {
  return request.get('/api/attribute/v1/get/by-category-and-type', { params: { attributeId } })
}

/**
 * 创建属性
 */
export const createAttribute = (requestParam) => {
  return request.post('/api/attribute/v1/create', requestParam)
}

/**
 * 更新属性
 */
export const updateAttribute = (requestParam) => {
  return request.post('/api/attribute/v1/update', requestParam)
}

/**
 * 删除属性
 */
export const deleteAttribute = (id) => {
  return request.post('/api/attribute/v1/delete', null, id)
}

/**
 * 批量删除属性
 */
export const batchDeleteAttributes = (ids) => {
  return request.post('/api/attribute/v1/delete/batch', ids)
}

/**
 * 根据ID查询属性详情
 */
export const getAttributeById = (id) => {
  return request.get('/api/attribute/v1/get', { params: { id } })
}

/**
 * 根据分类ID查询属性列表
 */
export const getAttributesByCategoryId = (categoryId) => {
  return request.get('/api/attribute/v1/get/by-category', { params: { categoryId } })
}

/**
 * 根据分类ID和属性类型查询属性列表
 */
export const getAttributesByCategoryIdAndType = (categoryId, attrType) => {
  return request.get('/api/attribute/v1/get/by-category-and-type', { params: { categoryId, attrType } })
}
