import request from './request'

/**
 * 添加商品到购物车
 */
export const addToCart = (data) => {
  return request.post('/api/cart/items', data)
}

/**
 * 修改购物车商品数量
 */
export const updateCartItem = (skuId, data) => {
  return request.put(`/api/cart/items/${skuId}`, data)
}

/**
 * 删除购物车商品
 */
export const removeFromCart = (skuId) => {
  return request.delete(`/api/cart/items/${skuId}`)
}

/**
 * 批量删除购物车商品
 */
export const batchRemoveFromCart = (skuIds) => {
  return request.post('/api/cart/items/delete-batch', skuIds)
}

/**
 * 清空购物车
 */
export const clearCart = () => {
  return request.delete('/api/cart')
}

/**
 * 获取购物车
 */
export const getCart = () => {
  return request.get('/api/cart')
}

/**
 * 更新单个购物车商品选中状态
 */
export const selectCartItem = (skuId, selected) => {
  return request.post(`/api/cart/items/${skuId}/selected`, { selected })
}

/**
 * 全选或取消全选购物车商品
 */
export const selectAllCartItems = (selected) => {
  return request.post('/api/cart/select-all', { selected })
}

/**
 * 获取购物车结算预览
 */
export const getCartCheckoutPreview = () => {
  return request.get('/api/cart/checkout-preview')
}
