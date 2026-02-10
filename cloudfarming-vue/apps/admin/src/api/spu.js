import request from './request'

/**
 * 创建SPU
 */
export const saveSpu = (data) => {
  return request.post('/api/spu/create', data)
}

/**
 * 根据id删除SPU
 */
export const deleteSpuById = (id) => {
  return request.post(`/api/spu/delete${id}`)
}

/**
 * 根据id获取单个SPU详情
 */
export const getSpuDetail = (id) => {
  return request.get(`/api/spu/get`,{
    params: {id}
  })
}

/**
 * 分页查询SPU列表
 */
export const listSpuByPage = (data) => {
  return request.post('/api/spu/page', data)
}

/**
 * 更新SPU状态
 */
export const updateSpuStatus = (id, status) => {
  return request.post(`/api/spu/status`, {
    id: id,
    status: status
  })
}

/**
 * 创建SPU属性值
 */
export const createSpuAttrValue = (data) => {
  return request.post('/api/spu/attr/create', data)
}

/**
 * 批量创建SPU属性值
 */
export const batchCreateSpuAttrValues = (data) => {
  return request.post('/api/spu/attr/create/batch', data)
}

/**
 * 更新SPU属性值
 */
export const updateSpuAttrValue = (data) => {
  return request.post(`/api/spu/attr/update`, data)
}

/**
 * 删除SPU属性值
 */
export const deleteSpuAttrValue = (id) => {
  return request.post(`/api/spu/attr/delete/${id}`)
}

/**
 * 批量删除SPU属性值
 */
export const batchDeleteSpuAttrValues = (ids) => {
  return request.post('/api/spu/attr/delete/batch', ids)
}

/**
 * 根据SPU ID删除所有属性值
 */
export const deleteSpuAttrValuesBySpuId = (spuId) => {
  return request.post(`/api/spu/attr/delete/all/${spuId}`)
}

/**
 * 根据ID查询SPU属性值
 */
export const getSpuAttrValueById = (id) => {
  return request.get(`/api/spu/attr/get/${id}`)
}

/**
 * 根据SPU ID查询属性值列表
 */
export const listSpuAttrValuesBySpuId = (spuId) => {
  return request.get('/v1/spu/attr/ list', { params: { spuId } })
}

/**
 * 根据SPU ID和属性ID查询属性值
 */
export const getSpuAttrValueBySpuIdAndAttrId = (spuId, attrId) => {
  return request.get('/v1/spu/attr/id', { params: { spuId, attrId } })
}
