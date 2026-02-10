import request from './request'

/**
 * 根据属性ID查询属性值列表
 * @param {string} attributeId
 * @returns {Promise<Object>} Result<AttributeValueRespDTO[]>
 */
export const getAttributeValuesByAttrId = (attributeId) => {
  return request.get('/api/attribute/v1/get/by-category-and-type', { params: { attributeId } })
}

/**
 * 创建属性
 * @param {Object} requestParam - AttributeCreateReqDTO
 * @returns {Promise<Object>} Result<void>
 */
export const createAttribute = (requestParam) => {
  return request.post('/api/attribute/v1/create', requestParam)
}

/**
 * 更新属性
 * @param {Object} requestParam - AttributeUpdateReqDTO
 * @returns {Promise<Object>} Result<boolean>
 */
export const updateAttribute = (requestParam) => {
  return request.post('/api/attribute/v1/update', requestParam)
}

/**
 * 删除属性
 * @param {string} id
 * @returns {Promise<Object>} Result<boolean>
 */
export const deleteAttribute = (id) => {
  return request.post('/api/attribute/v1/delete', null, { params: { id } })
}

/**
 * 批量删除属性
 * @param {Array<string>} ids
 * @returns {Promise<Object>} Result<boolean>
 */
export const batchDeleteAttributes = (ids) => {
  return request.post('/api/attribute/v1/delete/batch', ids)
}

/**
 * 根据ID查询属性详情
 * @param {string} id
 * @returns {Promise<Object>} Result<AttributeRespDTO>
 */
export const getAttributeById = (id) => {
  return request.get('/api/attribute/v1/get', { params: { id } })
}

/**
 * 根据分类ID查询属性列表
 * @param {string} categoryId
 * @returns {Promise<Object>} Result<AttributeRespDTO[]>
 */
export const getAttributesByCategoryId = (categoryId) => {
  return request.get('/api/attribute/v1/get/by-category', { params: { categoryId } })
}

/**
 * 根据分类ID和属性类型查询属性列表
 * @param {string} categoryId
 * @param {number} attrType
 * @returns {Promise<Object>} Result<AttributeRespDTO[]>
 */
export const getAttributesByCategoryIdAndType = (categoryId, attrType) => {
  return request.get('/api/attribute/v1/get/by-category-and-type', { params: { categoryId, attrType } })
}
