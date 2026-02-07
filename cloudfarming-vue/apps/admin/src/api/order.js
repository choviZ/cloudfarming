import request from './request'

/**
 * 查询订单列表
 */
export const listOrders = async (data) =>{
  return request.post('/api/order/v1/list/admin', data)
}