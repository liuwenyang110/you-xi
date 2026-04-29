import { uiStore } from '../store/ui'
import { handleError } from '../utils/errorHandler'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8080/api/v1'

// 类型定义
export interface RequestOptions {
  method?: string
  data?: any
  header?: Record<string, string>
  showErrorToast?: boolean
  retryCount?: number
}

export interface ApiError {
  code: number
  message: string
  data: any
}

function normalizeError(payload: any, response?: any): ApiError {
  const message = payload?.message
    || payload?.msg
    || (response && `请求失败（${response.status || response.statusCode}）`)
    || '请求失败'
  return {
    code: Number(payload?.code || response?.status || -1),
    message,
    data: payload?.data ?? null
  }
}

function handleAuthExpired(error: ApiError) {
  if (error.code !== 4001 && error.code !== 401) {
    return
  }
  uiStore.clearSession()
  if (typeof uni !== 'undefined' && typeof uni.reLaunch === 'function') {
    uni.showToast({ title: '登录已失效，请重新登录', icon: 'none' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/common/login' })
    }, 300)
  }
}

// 请求缓存（简单内存缓存）
const requestCache = new Map<string, { data: any; timestamp: number }>()
const CACHE_TTL = 5 * 60 * 1000 // 5分钟

/**
 * 增强的请求函数
 * @param url 请求路径
 * @param options 请求选项
 * @returns Promise<T>
 */
export function request<T = any>(url: string, options: RequestOptions = {}): Promise<T> {
  const {
    method = 'GET',
    data,
    header = {},
    showErrorToast = true,
    retryCount = 0
  } = options

  // 缓存键（仅GET请求考虑缓存）
  const cacheKey = method === 'GET' ? `${url}:${JSON.stringify(data || {})}` : null
  if (cacheKey) {
    const cached = requestCache.get(cacheKey)
    if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
      return Promise.resolve(cached.data)
    }
  }

  const isJsonBody = method !== 'GET' && data && typeof data === 'object'
  const headers = {
    'Content-Type': 'application/json',
    ...(uiStore.token ? { Authorization: `Bearer ${uiStore.token}` } : {}),
    ...header
  }

  // 重试逻辑
  const executeRequest = (attempt = 0): Promise<T> => {
    return new Promise((resolve, reject) => {
      const handleSuccess = (responseData: T) => {
        if (cacheKey) {
          requestCache.set(cacheKey, { data: responseData, timestamp: Date.now() })
        }
        resolve(responseData)
      }

      const handleErrorWrapper = (error: any) => {
        // 认证过期处理
        handleAuthExpired(error)
        
        // 错误处理
        if (showErrorToast) {
          handleError(error, { showToast: true, logToConsole: true })
        }
        
        // 重试逻辑
        if (attempt < retryCount && error.code !== 401 && error.code !== 403) {
          setTimeout(() => {
            executeRequest(attempt + 1).then(resolve).catch(reject)
          }, 1000 * (attempt + 1))
          return
        }
        
        reject(error)
      }

      // 平台判断：H5使用fetch，小程序使用uni.request
      if (typeof window !== 'undefined' && typeof fetch === 'function') {
        fetch(`${BASE_URL}${url}`, {
          method,
          headers,
          body: isJsonBody ? JSON.stringify(data) : undefined
        }).then(async (response) => {
          const payload = await response.json().catch(() => ({}))
          if (response.ok && payload.code === 0) {
            handleSuccess(payload.data)
          } else {
            handleErrorWrapper(normalizeError(payload, response))
          }
        }).catch(handleErrorWrapper)
      } else {
        uni.request({
          url: `${BASE_URL}${url}`,
          method,
          data: isJsonBody ? JSON.stringify(data) : (data || {}),
          header: headers,
          success: (res) => {
            const payload = res.data || {}
            if (res.statusCode >= 200 && res.statusCode < 300 && payload.code === 0) {
              handleSuccess(payload.data)
              return
            }
            handleErrorWrapper(normalizeError(payload, res))
          },
          fail: (error) => handleErrorWrapper(normalizeError(error))
        })
      }
    })
  }

  return executeRequest()
}

/**
 * 清除请求缓存
 */
export function clearRequestCache() {
  requestCache.clear()
}

/**
 * 移除特定缓存
 */
export function removeCache(key: string) {
  requestCache.delete(key)
}