import axios, { type AxiosInstance, type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '../stores/auth'
import router from '../router'

// 扩展 AxiosRequestConfig 类型以支持自定义属性
interface CustomAxiosRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean
}

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 是否正在刷新 token 的标志
let isRefreshing = false
// 重试请求队列
let refreshSubscribers: ((token: string) => void)[] = []

// 添加到重试队列
const subscribeTokenRefresh = (cb: (token: string) => void) => {
  refreshSubscribers.push(cb)
}

// 执行重试队列
const onRefreshed = (token: string) => {
  refreshSubscribers.forEach(cb => cb(token))
  refreshSubscribers = []
}

// 请求拦截器 - 添加 token
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理 401 和 token 更新
request.interceptors.response.use(
  (response) => {
    // 检查响应头中是否有新 token（后端被动续期时返回）
    const newToken = response.headers['x-new-token']
    if (newToken) {
      localStorage.setItem('token', newToken)
    }
    return response
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as CustomAxiosRequestConfig

    if (!originalRequest) {
      return Promise.reject(error)
    }

    // 处理 401 未授权
    if (error.response?.status === 401 && !originalRequest._retry) {
      // 如果是续期请求本身失败，直接跳转登录
      if (originalRequest.url?.includes('/auth/renew')) {
        const authStore = useAuthStore()
        authStore.logout()
        router.push({
          path: '/login',
          query: { redirect: router.currentRoute.value.fullPath }
        })
        return Promise.reject(error)
      }

      // 如果正在刷新 token，将请求加入队列
      if (isRefreshing) {
        return new Promise((resolve) => {
          subscribeTokenRefresh((token: string) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            resolve(request(originalRequest))
          })
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        // 尝试刷新 token
        const token = localStorage.getItem('token')
        const response = await axios.post('/api/auth/renew', null, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })

        const newToken = response.data.data
        if (newToken) {
          // 保存新 token
          localStorage.setItem('token', newToken)

          // 更新过期时间
          const expiresAt = Date.now() + 30 * 60 * 1000
          localStorage.setItem('tokenExpiresAt', String(expiresAt))

          // 执行重试队列
          onRefreshed(newToken)

          // 重试原请求
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          return request(originalRequest)
        }
      } catch (refreshError) {
        // 刷新失败，清除本地状态并跳转到登录页
        const authStore = useAuthStore()
        authStore.logout()
        router.push({
          path: '/login',
          query: { redirect: router.currentRoute.value.fullPath }
        })
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default request
