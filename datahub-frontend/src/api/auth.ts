import { request } from '@/utils/request'

/**
 * 登录请求参数
 */
export interface LoginRequest {
  username: string
  password: string
}

/**
 * 登录响应数据
 */
export interface LoginResponse {
  token: string
  userId: number
  username: string
  nickname: string
  avatar?: string
}

/**
 * 用户认证API
 */
export const authApi = {
  /**
   * 用户登录
   */
  login(data: LoginRequest) {
    return request.post<LoginResponse>('/auth/login', data)
  },

  /**
   * 用户登出
   */
  logout() {
    return request.post('/auth/logout')
  },

  /**
   * 获取当前用户信息
   */
  getUserInfo() {
    return request.get<LoginResponse>('/auth/info')
  },
}

