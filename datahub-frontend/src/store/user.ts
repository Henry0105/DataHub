import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api'

export interface UserInfo {
  id: string
  username: string
  nickname: string
  avatar?: string
  email?: string
  roles: string[]
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  // 设置token
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 设置用户信息
  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
  }

  // 登录
  const login = async (username: string, password: string) => {
    const res = await authApi.login({ username, password })
    setToken(res.token)
    setUserInfo({
      id: String(res.userId),
      username: res.username,
      nickname: res.nickname,
      avatar: res.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
      email: `${res.username}@datahub.com`,
      roles: ['admin'],
    })
  }

  // 登出
  const logout = async () => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
    }
  }

  // 获取用户信息
  const getUserInfo = async () => {
    const res = await authApi.getUserInfo()
    setUserInfo({
      id: String(res.userId),
      username: res.username,
      nickname: res.nickname,
      avatar: res.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
      email: `${res.username}@datahub.com`,
      roles: ['admin'],
    })
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    login,
    logout,
    getUserInfo,
  }
})

