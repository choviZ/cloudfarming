import request from './request'
import type { UserRegisterReqDTO, Result, UserRespDTO } from '../types/userType.ts'

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