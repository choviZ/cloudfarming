import request from './request'

/**
 * 添加收货地址
 * @param {Object} data - ReceiveAddressAddReq
 * @returns {Promise<Object>} Result<boolean>
 */
export function addReceiveAddress(data) {
    return request.post(
        '/api/receive-address/add',
        data
    )
}

/**
 * 修改收货地址
 * @param {Object} data - ReceiveAddressUpdateReq
 * @returns {Promise<Object>} Result<boolean>
 */
export function updateReceiveAddress(data) {
    return request.put(
        '/api/receive-address/update',
        data
    )
}

/**
 * 设置默认收货地址
 * @param {Object} data - ReceiveAddressSetDefaultReq
 * @returns {Promise<Object>} Result<boolean>
 */
export function setDefaultReceiveAddress(data) {
    return request.post(
        '/api/receive-address/default',
        data
    )
}

/**
 * 删除收货地址
 * @param {number} id
 * @returns {Promise<Object>} Result<boolean>
 */
export function deleteReceiveAddress(id) {
    return request.post(
        `/api/receive-address/delete/${id}`
    )
}

/**
 * 根据ID获取收货地址详情
 * @param {number} id
 * @returns {Promise<Object>} Result<ReceiveAddressResp>
 */
export function getReceiveAddressById(id) {
    return request.get(`/api/receive-address/get`,{params:{id}})
}

/**
 * 获取当前登录用户的所有收货地址
 * @returns {Promise<Object>} Result<ReceiveAddressResp[]>
 */
export function getCurrentUserReceiveAddresses() {
    return request.get(
        '/api/receive-address/list'
    )
}

/**
 * 获取当前登录用户的默认收货地址
 * @returns {Promise<Object>} Result<ReceiveAddressResp>
 */
export function getCurrentUserDefaultReceiveAddress() {
    return request.get(
        '/api/receive-address/default'
    )
}
