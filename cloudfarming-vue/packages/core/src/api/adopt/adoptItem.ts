import request from '../request'
import type {
    AdoptItemCreateReq,
    AdoptItemUpdateReq,
    AdoptItemPageReq,
    AdoptItemResp
} from './types'
import type { Result } from '../common'
import type { IPage } from '../common'

/**
 * 创建认养项目
 */
export function createAdoptItem(data: AdoptItemCreateReq) :Promise<Result<string>>{
    return request.post(
        '/api/adopt/item/v1/create',
        data
    )
}

/**
 * 修改认养项目基础信息
 */
export function updateAdoptItem(data: AdoptItemUpdateReq) :Promise<Result<void>>{
    return request.put(
        '/api/adopt/item/v1/update',
        data
    )
}

/**
 * 上架认养项目
 */
export function onShelfAdoptItem(adoptItemId: string): Promise<Result<void>> {
    return request.put(
        `/api/adopt/item/v1/${adoptItemId}/on-shelf`
    )
}

/**
 * 下架认养项目
 */
export function offShelfAdoptItem(adoptItemId: string): Promise<Result<void>> {
    return request.put(
        `/api/adopt/item/v1/${adoptItemId}/off-shelf`
    )
}

/**
 * 删除认养项目
 */
export function deleteAdoptItem(adoptItemId: string): Promise<Result<void>> {
    return request.delete(
        `/api/adopt/item/v1/${adoptItemId}`
    )
}

/**
 * 查询认养项目详情
 */
export function getAdoptItemDetail(adoptItemId: string): Promise<Result<AdoptItemResp>> {
    return request.get(
        `/api/adopt/item/v1/${adoptItemId}`
    )
}

/**
 * 分页查询认养项目
 */
export function pageAdoptItems(data: AdoptItemPageReq): Promise<Result<IPage<AdoptItemResp>>> {
    return request.post(
        '/api/adopt/item/v1/page',
        data
    )
}

/**
 * 查询我的发布（分页）
 */
export function pageMyAdoptItems(data: AdoptItemPageReq) : Promise<Result<IPage<AdoptItemResp>>> {
    return request.post(
        '/api/adopt/item/v1/my/page',
        data
    )
}
