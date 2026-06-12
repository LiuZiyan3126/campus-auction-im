import axios from 'axios'
import type { AxiosError, InternalAxiosRequestConfig } from 'axios'
import { useUserStore } from '@/stores/useUserStore'
import router from '@/router'
import { logger } from '@/utils/logger'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const userStore = useUserStore()
  if (userStore.accessToken) {
    config.headers.Authorization = `Bearer ${userStore.accessToken}`
  }
  return config
})

request.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    }
    logger.error('API请求失败:', error.config?.url, error.message)
    return Promise.reject(error)
  },
)

export default request
