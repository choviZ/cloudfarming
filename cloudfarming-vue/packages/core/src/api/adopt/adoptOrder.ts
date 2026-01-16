import request from '../request'
import type {
    AdoptOrderCreateReq,
    AdoptOrderResp
} from './types'
import type { Result } from '../common'


/**
 * 创建认养订单
 */
export function createAdoptOrder(data: AdoptOrderCreateReq) : Promise<Result<AdoptOrderResp>> {
    return request.post(
        '/v1/adopt/order',
        data
    )
}
