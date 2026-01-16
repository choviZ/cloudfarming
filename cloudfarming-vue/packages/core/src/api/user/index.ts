// 请求方法
import request from '../request.ts'
import type { Result, IPage } from '../common'
import type { 
  UserRegisterReqDTO, 
  UserRespDTO, 
  UserCreateReqDTO, 
  UserUpdateReqDTO, 
  UserPageQueryReqDTO 
} from './types'

// 用户注册
export const userRegister = (data: UserRegisterReqDTO): Promise<Result<boolean>> => {
  return request.post('/v1/user/register', data)
}

// 用户登录
export const userLogin = (data: { username: string; password: string }): Promise<Result<UserRespDTO>> => {
  return request.post('/v1/user/login', data)
}

// 获取登录用户信息
export const getUser = (): Promise<Result<UserRespDTO>> => {
  return request.get('/v1/user')
}

// 退出登录
export const userLogout = (): Promise<Result<boolean>> => {
  return request.get('/v1/user/logout')
}

// 根据id获取用户信息
export const getUserById = (id: number): Promise<Result<UserRespDTO>> => {
  return request.get(`/admin/v1/user/${id}`)
}

// 创建用户
export const createUser = (data: UserCreateReqDTO): Promise<Result<boolean>> => {
  return request.post('/admin/v1/user', data)
}

// 修改用户信息
export const updateUser = (data: UserUpdateReqDTO): Promise<Result<boolean>> => {
  return request.put('/admin/v1/user', data)
}

// 删除用户
export const deleteUser = (id: string): Promise<Result<boolean>> => {
  return request.delete(`/admin/v1/user/${id}`)
}

// 分页查询用户列表
export const getUserPage = (data: UserPageQueryReqDTO): Promise<Result<IPage<UserRespDTO>>> => {
  return request.post('/admin/v1/user/page', data)
}

// 导出类型
export * from './types'