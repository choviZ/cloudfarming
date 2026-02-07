import request from './request'

/**
 * 查询订单列表
 */
export const listOrders = async (data) =>{
  return request.post('/api/order/v1/list/admin', data)
}

/**
 * 查询领养订单详情
 */
export const getAdoptOrderDetail = async (params) =>{
  return request.get('/api/order/v1/detail/adopt', { params })
}

/**
 * 查询商品订单详情
 */
export const getSkuOrderDetail = async (params) =>{
  return request.get('/api/order/v1/detail/sku', { params })
}