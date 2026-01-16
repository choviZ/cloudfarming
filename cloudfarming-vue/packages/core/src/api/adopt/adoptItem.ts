import request from '../request'
import type {
    AdoptItemCreateReq,
    AdoptItemUpdateReq,
    AdoptItemPageReq,
    AdoptItemResp
} from './types'
import type { Result } from '../../types/common'
import type { IPage } from '../../types/common'

/**
 * 创建认养项目
 */
export function createAdoptItem(data: AdoptItemCreateReq) :Promise<Result<string>>{
    return request.post(
        '/v1/adopt-item',
        data
    )
}

/**
 * 修改认养项目基础信息
 */
export function updateAdoptItem(data: AdoptItemUpdateReq) :Promise<Result<void>>{
    return request.put(
        '/v1/adopt-item',
        data
    )
}

/**
 * 上架认养项目
 */
export function onShelfAdoptItem(adoptItemId: string): Promise<Result<void>> {
    return request.put(
        `/v1/adopt-item/${adoptItemId}/on-shelf`
    )
}

/**
 * 下架认养项目
 */
export function offShelfAdoptItem(adoptItemId: string): Promise<Result<void>> {
    return request.put(
        `/v1/adopt-item/${adoptItemId}/off-shelf`
    )
}

/**
 * 删除认养项目
 */
export function deleteAdoptItem(adoptItemId: string): Promise<Result<void>> {
    return request.delete(
        `/v1/adopt-item/${adoptItemId}`
    )
}

/**
 * 查询认养项目详情
 */
export function getAdoptItemDetail(adoptItemId: string): Promise<Result<AdoptItemResp>> {
    return request.get(
        `/v1/adopt-item/${adoptItemId}`
    )
}

/**
 * 分页查询认养项目
 */
export function pageAdoptItems(data: AdoptItemPageReq): Promise<Result<IPage<AdoptItemResp>>> {
    return request.post(
        '/v1/adopt-item/page',
        data
    )
}

/**
 * 查询我的发布（分页）
 */
export function pageMyAdoptItems(data: AdoptItemPageReq) : Promise<Result<IPage<AdoptItemResp>>> {
    return request.post(
        '/v1/adopt-item/my/page',
        data
    )
}
