import request from './request'

/**
 * 创建认养项目
 * @param {Object} data - AdoptItemCreateReq
 * @returns {Promise<Object>} Result<string>
 */
export function createAdoptItem(data) {
    return request.post(
        '/api/adopt/item/v1/create',
        data
    )
}

/**
 * 修改认养项目基础信息
 * @param {Object} data - AdoptItemUpdateReq
 * @returns {Promise<Object>} Result<void>
 */
export function updateAdoptItem(data) {
    return request.put(
        '/api/adopt/item/v1/update',
        data
    )
}

/**
 * 上架认养项目
 * @param {string} adoptItemId
 * @returns {Promise<Object>} Result<void>
 */
export function onShelfAdoptItem(adoptItemId) {
    return request.put(
        `/api/adopt/item/v1/${adoptItemId}/on-shelf`
    )
}

/**
 * 下架认养项目
 * @param {string} adoptItemId
 * @returns {Promise<Object>} Result<void>
 */
export function offShelfAdoptItem(adoptItemId) {
    return request.put(
        `/api/adopt/item/v1/${adoptItemId}/off-shelf`
    )
}

/**
 * 删除认养项目
 * @param {string} adoptItemId
 * @returns {Promise<Object>} Result<void>
 */
export function deleteAdoptItem(adoptItemId) {
    return request.delete(
        `/api/adopt/item/v1/${adoptItemId}`
    )
}

/**
 * 查询认养项目详情
 * @param {string} adoptItemId
 * @returns {Promise<Object>} Result<AdoptItemResp>
 */
export function getAdoptItemDetail(adoptItemId) {
    return request.get(
        `/api/adopt/item/v1/${adoptItemId}`
    )
}

/**
 * 分页查询认养项目
 * @param {Object} data - AdoptItemPageReq
 * @returns {Promise<Object>} Result<IPage<AdoptItemResp>>
 */
export function pageAdoptItems(data) {
    return request.post(
        '/api/adopt/item/v1/page',
        data
    )
}

/**
 * 查询我的发布（分页）
 * @param {Object} data - AdoptItemPageReq
 * @returns {Promise<Object>} Result<IPage<AdoptItemResp>>
 */
export function pageMyAdoptItems(data) {
    return request.post(
        '/api/adopt/item/v1/my/page',
        data
    )
}

/**
 * 查询我的养殖实例（分页）
 * @param {Object} data - AdoptInstancePageReq
 * @returns {Promise<Object>} Result<IPage<AdoptInstanceResp>>
 */
export function pageMyAdoptInstances(data) {
    return request.post(
        '/api/adopt/instance/v1/my',
        data
    )
}

/**
 * 查询养殖实例详情
 * @param {string|number} id
 * @returns {Promise<Object>} Result<AdoptInstanceDetailResp>
 */
export function getAdoptInstanceDetail(id) {
    return request.get('/api/adopt/instance/v1/detail', {
        params: { id }
    })
}

/**
 * 将养殖实例标记为异常死亡
 * @param {Object} data - AdoptInstanceMarkDeadReq
 * @returns {Promise<Object>} Result<void>
 */
export function markAdoptInstanceDead(data) {
    return request.post(
        '/api/adopt/instance/v1/dead',
        data
    )
}

/**
 * 创建养殖日志
 * @param {Object} data - AdoptLogCreateReq
 * @returns {Promise<Object>} Result<void>
 */
export function createAdoptLog(data) {
    return request.post(
        '/api/adopt/log/v1/create',
        data
    )
}

/**
 * 分页查询养殖日志
 * @param {Object} data - AdoptLogPageReq
 * @returns {Promise<Object>} Result<IPage<AdoptLogResp>>
 */
export function pageAdoptLogs(data) {
    return request.post(
        '/api/adopt/log/v1/page',
        data
    )
}

/**
 * 查询体重变化曲线
 * @param {string|number} instanceId
 * @returns {Promise<Object>} Result<AdoptWeightPointResp[]>
 */
export function getAdoptWeightTrend(instanceId) {
    return request.get('/api/adopt/log/v1/weight-trend', {
        params: { instanceId }
    })
}

/**
 * 创建认养订单
 * @param {Object} data - AdoptOrderCreateReq
 * @returns {Promise<Object>} Result<AdoptOrderResp>
 */
export function createAdoptOrder(data) {
    return request.post(
        '/v1/adopt/order',
        data
    )
}

/**
 * 获取动物分类列表
 * @returns {Promise<Object>} Result<AnimalCategoryResp[]>
 */
export function listAnimalCategories() {
  return request.get(
    '/api/adopt/animal-category/list'
  )
}
