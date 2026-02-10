import request from './request'

/**
 * 创建或修改SPU
 * @param {Object} data - SpuCreateOrUpdateReqDTO
 * @returns {Promise<Object>} Result<string>
 */
export const saveSpu = (data) => {
  return request.post('/api/spu/create', data)
}

/**
 * 根据id删除SPU
 * @param {number} id
 * @returns {Promise<Object>} Result<void>
 */
export const deleteSpuById = (id) => {
  return request.post(`/api/spu/delete${id}`)
}

/**
 * 根据id获取单个SPU详情
 * @param {number} id
 * @returns {Promise<Object>} Result<SpuDetailRespDTO>
 */
export const getSpuDetail = (id) => {
  return request.get(`/api/spu/get`, { params: { id } })
}

/**
 * 分页查询SPU列表
 * @param {Object} data - SpuPageQueryReqDTO
 * @returns {Promise<Object>} Result<IPage<SpuRespDTO>>
 */
export const listSpuByPage = (data) => {
  return request.post('/api/spu/page', data)
}

/**
 * 更新SPU状态
 * @param {number} id
 * @param {number} status
 * @returns {Promise<Object>} Result<void>
 */
export const updateSpuStatus = (id, status) => {
  return request.put(`/api/spu/status/${id}`, null, {
    params: { status }
  })
}

/**
 * 创建SPU属性值
 * @param {Object} data - SpuAttrValueCreateReqDTO
 * @returns {Promise<Object>} Result<void>
 */
export const createSpuAttrValue = (data) => {
  return request.post('/api/spu/attr/create', data)
}

/**
 * 批量创建SPU属性值
 * @param {Array} data - SpuAttrValueCreateReqDTO[]
 * @returns {Promise<Object>} Result<void>
 */
export const batchCreateSpuAttrValues = (data) => {
  return request.post('/api/spu/attr/create/batch', data)
}

/**
 * 更新SPU属性值
 * @param {Object} data - SpuAttrValueUpdateReqDTO
 * @returns {Promise<Object>} Result<boolean>
 */
export const updateSpuAttrValue = (data) => {
  return request.post(`/api/spu/attr/update`, data)
}

/**
 * 删除SPU属性值
 * @param {number} id
 * @returns {Promise<Object>} Result<boolean>
 */
export const deleteSpuAttrValue = (id) => {
  return request.post(`/api/spu/attr/delete/${id}`)
}

/**
 * 批量删除SPU属性值
 * @param {Array<number>} ids
 * @returns {Promise<Object>} Result<boolean>
 */
export const batchDeleteSpuAttrValues = (ids) => {
  return request.post('/api/spu/attr/delete/batch', ids)
}

/**
 * 根据SPU ID删除所有属性值
 * @param {number} spuId
 * @returns {Promise<Object>} Result<boolean>
 */
export const deleteSpuAttrValuesBySpuId = (spuId) => {
  return request.post(`/api/spu/attr/delete/all/${spuId}`)
}

/**
 * 根据ID查询SPU属性值
 * @param {number} id
 * @returns {Promise<Object>} Result<SpuAttrValueRespDTO>
 */
export const getSpuAttrValueById = (id) => {
  return request.get(`/api/spu/attr/get/${id}`)
}

/**
 * 根据SPU ID查询属性值列表
 * @param {number} spuId
 * @returns {Promise<Object>} Result<SpuAttrValueRespDTO[]>
 */
export const listSpuAttrValuesBySpuId = (spuId) => {
  return request.get('/v1/spu/attr/ list', { params: { spuId } })
}

/**
 * 根据SPU ID和属性ID查询属性值
 * @param {number} spuId
 * @param {number} attrId
 * @returns {Promise<Object>} Result<SpuAttrValueRespDTO>
 */
export const getSpuAttrValueBySpuIdAndAttrId = (spuId, attrId) => {
  return request.get('/v1/spu/attr/id', { params: { spuId, attrId } })
}
