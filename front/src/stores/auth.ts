import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../utils/request'

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

  // 启动续期定时器（每 10 分钟检查一次）
  const startRenewTimer = (expiresIn: number = 30 * 60 * 1000) => {
    clearRenewTimer()
    const renewTime = 10 * 60 * 1000 // 10 分钟后检查续期
    console.log(`[Token 续期] 定时器启动，将在 10 分钟后 (${new Date(Date.now() + renewTime).toLocaleTimeString()}) 检查续期`)
    renewTimer = setTimeout(() => {
      console.log('[Token 续期] 定时器触发，开始续期')
      renewToken()
    }, renewTime)
  }

  // 重置定时器（续期成功后调用）
  const resetRenewTimer = () => {
    startRenewTimer()
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
      console.log('[Token 续期] 发送续期请求')
      const response = await request.post('/auth/renew')
      const newToken = response.data.data
      if (newToken) {
        console.log('[Token 续期] 续期成功，新 token:', newToken.substring(0, 20) + '...')
        token.value = newToken
        localStorage.setItem('token', newToken)
        // 重置定时器（25 分钟后再次续期）
        startRenewTimer()
      } else {
        console.warn('[Token 续期] 续期返回为空')
        logout()
      }
    } catch (error: any) {
      // 401 错误已经在 request.ts 中处理，会跳转登录
      console.error('Token 续期失败:', error.message)
      // 不立即 logout，让 request.ts 的 401 处理器处理
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

    // 启动续期定时器（无论是否过期都启动，过期了会在请求时自动续期）
    if (savedToken) {
      startRenewTimer()
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
    clearRenewTimer,
    resetRenewTimer
  }
})
