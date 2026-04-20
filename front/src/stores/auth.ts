import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

interface LoginRequest {
  username: string
  password: string
  rememberMe?: boolean
}

interface LoginResponse {
  token: string
  userId: number
  username: string
  expiresIn: number
}

interface UserInfo {
  userId: number
  username: string
}

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const loading = ref(false)

  // Getters
  const isLoggedIn = computed(() => !!token.value)

  // Actions
  const login = async (loginData: LoginRequest): Promise<boolean> => {
    loading.value = true
    try {
      const response = await request.post<LoginResponse>('/auth/login', loginData)
      const { token: newToken, userId, username } = response.data

      // 保存 token
      token.value = newToken
      localStorage.setItem('token', newToken)

      // 保存用户信息
      userInfo.value = { userId, username }
      localStorage.setItem('userInfo', JSON.stringify({ userId, username }))

      return true
    } catch (error) {
      console.error('登录失败:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  const logout = async (): Promise<void> => {
    try {
      // 调用后端登出接口
      if (token.value) {
        await request.post('/auth/logout')
      }
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      // 清除本地状态
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }

  const initAuth = (): void => {
    // 从 localStorage 恢复认证状态
    const savedToken = localStorage.getItem('token')
    const savedUserInfo = localStorage.getItem('userInfo')

    if (savedToken) {
      token.value = savedToken
    }

    if (savedUserInfo) {
      try {
        userInfo.value = JSON.parse(savedUserInfo)
      } catch {
        userInfo.value = null
      }
    }
  }

  return {
    token,
    userInfo,
    loading,
    isLoggedIn,
    login,
    logout,
    initAuth
  }
})
