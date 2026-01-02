import request from '../request.ts'
import type { Result } from '../../types/common'
import type { UserRegisterReqDTO, UserRespDTO } from '../../types/user'

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