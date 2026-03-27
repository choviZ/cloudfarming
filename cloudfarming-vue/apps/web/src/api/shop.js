import request from './request'

export const getShopInfo = (shopId) => {
  return request.get('/api/shop/get', {
    params: { shopId }
  })
}

export const getMyShopInfo = () => {
  return request.get('/api/shop/my')
}

export const updateShopInfo = (data) => {
  return request.post('/api/shop/update', data)
}
