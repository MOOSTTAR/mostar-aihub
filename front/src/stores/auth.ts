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
  const tokenExpiresAt = ref<number | null>(null)

  // 续期定时器
  let renewTimer: ReturnType<typeof setTimeout> | null = null

  // Getters
  const isLoggedIn = computed(() => !!token.value)

  // 启动续期定时器（提前 5 分钟续期）
  const startRenewTimer = (expiresIn: number = 30 * 60 * 1000) => {
    clearRenewTimer()
    const renewTime = expiresIn - 5 * 60 * 1000 // 提前 5 分钟
    if (renewTime > 0) {
      renewTimer = setTimeout(() => {
        renewToken()
      }, renewTime)
    }
  }

  // 清除定时器
  const clearRenewTimer = () => {
    if (renewTimer) {
      clearTimeout(renewTimer)
      renewTimer = null
    }
  }

  // 主动续期 token
  const renewToken = async (): Promise<void> => {
    if (!token.value) return

    try {
      const response = await request.post('/auth/renew')
      const newToken = response.data.data
      if (newToken) {
        token.value = newToken
        localStorage.setItem('token', newToken)
        // 重置定时器（30 分钟后再次续期）
        startRenewTimer()
      }
    } catch (error) {
      console.error('Token 续期失败:', error)
      // 续期失败，清除本地状态并跳转到登录页
      logout()
    }
  }

  // Actions
  const login = async (loginData: LoginRequest): Promise<boolean> => {
    loading.value = true
    try {
      const response = await request.post<LoginResponse>('/auth/login', loginData)
      const { token: newToken, userId, username, expiresIn } = response.data

      // 保存 token
      token.value = newToken
      localStorage.setItem('token', newToken)

      // 保存用户信息
      userInfo.value = { userId, username }
      localStorage.setItem('userInfo', JSON.stringify({ userId, username }))

      // 保存过期时间（毫秒）
      const expiresAt = Date.now() + expiresIn * 1000
      tokenExpiresAt.value = expiresAt
      localStorage.setItem('tokenExpiresAt', String(expiresAt))

      // 启动续期定时器
      startRenewTimer(expiresIn * 1000)

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
      clearRenewTimer()
      token.value = ''
      userInfo.value = null
      tokenExpiresAt.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('tokenExpiresAt')
    }
  }

  const initAuth = (): void => {
    // 从 localStorage 恢复认证状态
    const savedToken = localStorage.getItem('token')
    const savedUserInfo = localStorage.getItem('userInfo')
    const savedExpiresAt = localStorage.getItem('tokenExpiresAt')

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

    // 恢复过期时间
    if (savedExpiresAt) {
      tokenExpiresAt.value = Number(savedExpiresAt)
    }

    // 如果 token 未过期，启动续期定时器
    if (savedToken && savedExpiresAt) {
      const expiresAt = Number(savedExpiresAt)
      const remaining = expiresAt - Date.now()
      if (remaining > 5 * 60 * 1000) {
        // 剩余时间大于 5 分钟，启动定时器
        startRenewTimer(remaining)
      } else if (remaining > 0) {
        // 剩余时间不足 5 分钟，立即续期
        renewToken()
      } else {
        // token 已过期，清除状态
        logout()
      }
    }
  }

  return {
    token,
    userInfo,
    loading,
    isLoggedIn,
    tokenExpiresAt,
    login,
    logout,
    initAuth,
    renewToken,
    clearRenewTimer
  }
})
