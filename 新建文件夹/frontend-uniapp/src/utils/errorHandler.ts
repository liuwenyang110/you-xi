import { showToast } from './ui'

// 错误类型定义
export interface AppError {
  code: number | string
  message: string
  details?: any
}

// 统一错误处理配置
export interface ErrorHandlerOptions {
  showToast?: boolean
  logToConsole?: boolean
  throwError?: boolean
}

/**
 * 统一错误处理函数
 * @param error 错误对象
 * @param options 处理选项
 */
export function handleError(
  error: any,
  options: ErrorHandlerOptions = {}
): AppError {
  const defaultOptions: ErrorHandlerOptions = {
    showToast: true,
    logToConsole: true,
    throwError: false
  }
  const opts = { ...defaultOptions, ...options }

  // 标准化错误信息
  const appError: AppError = normalizeError(error)

  // 控制台日志
  if (opts.logToConsole) {
    console.error('[App Error]', appError)
  }

  // 显示用户提示
  if (opts.showToast) {
    showToast(appError.message || '网络请求失败，请稍后重试')
  }

  // 可选择重新抛出错误
  if (opts.throwError) {
    throw appError
  }

  return appError
}

/**
 * 将各种错误格式标准化为 AppError
 */
function normalizeError(error: any): AppError {
  // 如果是已经标准化的错误
  if (error?.code !== undefined && error?.message !== undefined) {
    return error as AppError
  }

  // 网络错误（fetch失败）
  if (error instanceof TypeError && error.message.includes('Network')) {
    return {
      code: 'NETWORK_ERROR',
      message: '网络连接失败，请检查网络设置'
    }
  }

  // HTTP错误（响应状态码非2xx）
  if (error?.response?.status) {
    const status = error.response.status
    let message = '请求失败'
    if (status === 401) message = '登录已过期，请重新登录'
    if (status === 403) message = '权限不足'
    if (status === 404) message = '请求的资源不存在'
    if (status === 500) message = '服务器内部错误'
    
    return {
      code: `HTTP_${status}`,
      message,
      details: error.response.data
    }
  }

  // 通用错误
  return {
    code: 'UNKNOWN_ERROR',
    message: error?.message || '未知错误',
    details: error
  }
}

/**
 * 创建错误处理钩子（用于Vue组件）
 */
export function useErrorHandler() {
  return {
    handleError
  }
}