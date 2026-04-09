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
  BREEDING: 6,
  PENDING_ASSIGNMENT: 7
}

export const ORDER_STATUS_TEXT = {
  0: '待付款',
  1: '待发货',
  2: '已发货',
  3: '已完成',
  4: '已关闭',
  5: '售后中',
  6: '养殖中',
  7: '待分配'
}

export const getOrderList = (data) => {
  return request.post('/api/order/v1/list', data)
}

export const getPayOrderList = (data) => {
  return request.post('/api/pay/list', data)
}

export const confirmPayOrder = (payOrderNo) => {
  return request.get('/api/alipay/confirm', {
    params: { payOrderNo }
  })
}

export const getFarmerOrderStatistics = () => {
  return request.get('/api/order/v1/farmer/statistics')
}

export const getFarmerOrderList = (data) => {
  return request.post('/api/order/v1/farmer/list', data)
}

export const shipFarmerOrder = (data) => {
  return request.post('/api/order/v1/farmer/ship', data)
}

export const receiveUserOrder = (data) => {
  return request.post('/api/order/v1/receive', data)
}

export const getAdoptOrderDetail = (orderNo) => {
  return request.get('/api/order/v1/detail/adopt', {
    params: { orderNo }
  })
}

export const getSkuOrderDetail = (orderNo) => {
  return request.get('/api/order/v1/detail/sku', {
    params: { orderNo }
  })
}

export const assignFarmerAdoptOrder = (data) => {
  return request.post('/api/order/v1/farmer/adopt/assign', data)
}

export const fulfillFarmerAdoptInstance = (data) => {
  return request.post('/api/order/v1/farmer/adopt/fulfill', data)
}

export const getMyPendingReviewOrders = (data) => {
  return request.post('/api/order/review/v1/my/pending/page', data)
}

export const createOrderReview = (data) => {
  return request.post('/api/order/review/v1/create', data)
}

export const getSpuReviewSummary = (spuId) => {
  return request.get('/api/order/review/v1/spu/summary', {
    params: { spuId }
  })
}

export const pageSpuReviews = (data) => {
  return request.post('/api/order/review/v1/spu/page', data)
}
