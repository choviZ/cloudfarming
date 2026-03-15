import request from './request'

// 获取店铺信息
export const getShopInfo = (shopId) => {
    return request.get(`/api/shop/get`, { params: { shopId } })
}
