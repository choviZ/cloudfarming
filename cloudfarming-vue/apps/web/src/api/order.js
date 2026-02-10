import request from "./request";

/**
 * 创建订单
 * @param {Object} data - OrderCreateReq
 * @returns {Promise<Object>} Result<OrderCreateResp>
 */
export const createOrder = (data) => {
  return request.post('/api/order/v1/create', data)
}

export const ORDER_TYPE = {
  ADOPT: 0,
  GOODS: 1
}
