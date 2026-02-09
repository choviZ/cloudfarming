import request from './request'
import type { Result } from '../types/common.ts'
import type { UserRegisterReqDTO, UserRespDTO } from '@/types/userType.ts'

// 用户注册
export const userRegister = (data: UserRegisterReqDTO): Promise<Result<boolean>> => {
  return request.post('/api/user/register', data)
}

// 用户登录
export const userLogin = (data: { username: string; password: string }): Promise<Result<UserRespDTO>> => {
  return request.post('/api/user/login', data)
}

// 获取登录用户信息
export const getUser = (): Promise<Result<UserRespDTO>> => {
  return request.get('/api/user/get/login')
}

// 退出登录
export const userLogout = (): Promise<Result<boolean>> => {
  return request.get('/api/user/logout')
}