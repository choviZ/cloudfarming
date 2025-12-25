import request from '@/api/request';
import type { Result } from '@/types/common.ts'
import type { CartRespDTO, CartItemAddReqDTO, CartItemUpdateReqDTO } from '@/types/cart';

/**
 * 添加商品到购物车
 */
export const addToCart = (data: CartItemAddReqDTO): Promise<Result<boolean>> => {
    return request.post('/v1/cart/items', data);
};

/**
 * 更新购物车商品
 */
export const updateCartItem = (data: CartItemUpdateReqDTO): Promise<Result<boolean>> => {
    return request.put('/v1/cart/items', data);
};

/**
 * 删除购物车商品
 */
export const removeFromCart = (skuId: string): Promise<Result<boolean>> => {
    return request.delete(`/v1/cart/items/${skuId}`);
};

/**
 * 批量删除购物车商品
 */
export const batchRemoveFromCart = (skuIds: string[]): Promise<Result<boolean>> => {
    return request.delete('/v1/cart/items', { data: skuIds });
};

/**
 * 清空购物车
 */
export const clearCart = (): Promise<Result<boolean>> => {
    return request.delete('/v1/cart/clear');
};

/**
 * 获取购物车
 */
export const getCart = (): Promise<Result<CartRespDTO>> => {
    return request.get('/v1/cart');
};

/**
 * 选择/取消选择购物车商品
 */
export const selectCartItem = (skuId: string, selected: boolean): Promise<Result<boolean>> => {
    return request.put(`/v1/cart/items/${skuId}/select`, null, {
        params: { selected }
    });
};

/**
 * 全选/取消全选购物车商品
 */
export const selectAllCartItems = (selected: boolean): Promise<Result<boolean>> => {
    return request.put('/v1/cart/items/select-all', null, {
        params: { selected }
    });
};
