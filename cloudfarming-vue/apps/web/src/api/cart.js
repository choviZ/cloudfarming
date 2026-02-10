import request from './request';

/**
 * 添加商品到购物车
 * @param {Object} data - CartItemAddReqDTO
 */
export const addToCart = (data) => {
    return request.post('/api/cart/add', data);
};

/**
 * 更新购物车商品
 * @param {Object} data - CartItemUpdateReqDTO
 */
export const updateCartItem = (data) => {
    return request.post('/api/cart/update', data);
};

/**
 * 删除购物车商品
 * @param {string} skuId
 */
export const removeFromCart = (skuId) => {
    return request.get('/api/cart/delete', { params: { skuId } });
};

/**
 * 批量删除购物车商品
 * @param {Array<string>} skuIds
 */
export const batchRemoveFromCart = (skuIds) => {
    return request.post('/api/cart/delete-batch', skuIds);
};

/**
 * 清空购物车
 */
export const clearCart = () => {
    return request.get('/api/cart/clear');
};

/**
 * 获取购物车
 */
export const getCart = () => {
    return request.get('/api/cart/get');
};

/**
 * 选择/取消选择购物车商品
 * @param {string} skuId
 * @param {boolean} selected
 */
export const selectCartItem = (skuId, selected) => {
    return request.get('/api/cart/select', {
        params: { skuId, selected }
    });
};

/**
 * 全选/取消全选购物车商品
 * @param {boolean} selected
 */
export const selectAllCartItems = (selected) => {
    return request.get('/api/cart/select-all', {
        params: { selected }
    });
};
