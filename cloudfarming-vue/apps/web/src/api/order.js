import request from "./request";

/**
 * 创建订单
 * @param {Object} data - OrderCreateReq
 * @returns {Promise<Object>} Result<OrderCreateResp>
 */
export const createOrder = (data) => {
  return request.post('/api/order/v1/create', data, {
    timeout: 30000
  })
}

export const getCurrentAdoptAgreement = () => {
  return request.get('/api/order/v1/agreement/adopt/current')
}

export const ORDER_TYPE = {
  ADOPT: 0,
  GOODS: 1
}

export const ORDER_STATUS = {
  PENDING_PAYMENT: 0,
  PENDING_SHIPMENT: 1,
  SHIPPED: 2,
  COMPLETED: 3,
  CANCEL: 4,
  AFTER_SALE: 5,
  BREEDING: 6
}

export const ORDER_STATUS_TEXT = {
  0: '待付款',
  1: '待发货',
  2: '已发货',
  3: '已完成',
  4: '已关闭',
  5: '售后中',
  6: '养殖中'
}

export const getOrderList = (data) => {
  return request.post('/api/order/v1/list', data)
}

export const getPayOrderList = (data) => {
  return request.post('/api/pay/list', data)
}

export const getFarmerOrderStatistics = () => {
  return request.get('/api/order/v1/farmer/statistics')
}
