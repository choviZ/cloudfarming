import request from './request'

/**
 * 用户注册
 * @param {Object} data - UserRegisterReqDTO
 * @returns {Promise<Object>} Result<boolean>
 */
export const userRegister = (data) => {
  return request.post('/api/user/register', data)
}

/**
 * 用户登录
 * @param {Object} data - { username: string; password: string }
 * @returns {Promise<Object>} Result<UserRespDTO>
 */
export const userLogin = (data) => {
  return request.post('/api/user/login', data)
}

/**
 * 获取登录用户信息
 * @returns {Promise<Object>} Result<UserRespDTO>
 */
export const getUser = () => {
  return request.get('/api/user/get/login')
}

/**
 * 退出登录
 * @returns {Promise<Object>} Result<boolean>
 */
export const userLogout = () => {
  return request.get('/api/user/logout')
}

/**
 * 根据id获取用户信息
 * @param {number} id
 * @returns {Promise<Object>} Result<UserRespDTO>
 */
export const getUserById = (id) => {
  return request.get(`/api/user/get/${id}`)
}

/**
 * 创建用户
 * @param {Object} data - UserCreateReqDTO
 * @returns {Promise<Object>} Result<boolean>
 */
export const createUser = (data) => {
  return request.post('/api/user/create', data)
}

/**
 * 修改用户信息
 * @param {Object} data - UserUpdateReqDTO
 * @returns {Promise<Object>} Result<boolean>
 */
export const updateUser = (data) => {
  return request.post('/api/user/update', data)
}

/**
 * 删除用户
 * @param {string} id
 * @returns {Promise<Object>} Result<boolean>
 */
export const deleteUser = (id) => {
  return request.post(`/api/user/delete`,id)
}

/**
 * 分页查询用户列表
 * @param {Object} data - UserPageQueryReqDTO
 * @returns {Promise<Object>} Result<IPage<UserRespDTO>>
 */
export const getUserPage = (data) => {
  return request.post('/api/user/page', data)
}
