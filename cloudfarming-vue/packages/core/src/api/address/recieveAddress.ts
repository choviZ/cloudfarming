import request from '../request'
import type {
    ReceiveAddressAddReq,
    ReceiveAddressUpdateReq,
    ReceiveAddressResp,
    ReceiveAddressSetDefaultReq
} from './types'
import type { Result } from '../common'


/**
 * 添加收货地址
 */
export function addReceiveAddress(data: ReceiveAddressAddReq): Promise<Result<boolean>> {
    return request.post(
        '/v1/user/receive-address',
        data
    )
}

/**
 * 修改收货地址
 */
export function updateReceiveAddress(data: ReceiveAddressUpdateReq): Promise<Result<boolean>> {
    return request.put(
        '/v1/user/receive-address',
        data
    )
}

/**
 * 设置默认收货地址
 */
export function setDefaultReceiveAddress(data: ReceiveAddressSetDefaultReq): Promise<Result<boolean>> {
    return request.put(
        '/v1/user/receive-address/default',
        data
    )
}

/**
 * 删除收货地址
 */
export function deleteReceiveAddress(id: number): Promise<Result<boolean>> {
    return request.delete(
        `/v1/user/receive-address/${id}`
    )
}

/**
 * 根据ID获取收货地址详情
 */
export function getReceiveAddressById(id: number): Promise<Result<ReceiveAddressResp>> {
    return request.get(
        `/v1/user/receive-address/${id}`
    )
}

/**
 * 获取当前登录用户的所有收货地址
 */
export function getCurrentUserReceiveAddresses(): Promise<Result<ReceiveAddressResp[]>> {
    return request.get(
        '/v1/user/receive-address'
    )
}

/**
 * 获取当前登录用户的默认收货地址
 */
export function getCurrentUserDefaultReceiveAddress(): Promise<Result<ReceiveAddressResp>> {
    return request.get(
        '/v1/user/receive-address/default'
    )
}
