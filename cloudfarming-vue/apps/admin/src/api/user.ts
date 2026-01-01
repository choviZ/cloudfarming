import axios from 'axios';
import type { RouteRecordNormalized } from 'vue-router';
import type { UserState } from '@/store/modules/user/types';

// 登录数据
export interface LoginData {
  username: string;
  password: string;
}

export interface LoginRes {
  token: string;
}

/**
 * 用户登录
 * @param data
 */
export function login(data: LoginData) {
  return axios.post<LoginRes>('/api/user/login', data);
}

/**
 * 退出登录
 */
export function logout() {
  return axios.post<LoginRes>('/api/user/logout');
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  return axios.post<UserState>('/api/user/info');
}

export function getMenuList() {
  return axios.post<RouteRecordNormalized[]>('/api/user/menu');
}

